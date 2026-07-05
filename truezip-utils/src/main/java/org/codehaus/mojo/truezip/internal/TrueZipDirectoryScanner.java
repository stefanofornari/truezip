package org.codehaus.mojo.truezip.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.util.Vector;

import de.schlichtherle.truezip.file.TFile;

/**
 * Class for scanning a directory for files/directories which match certain criteria.
 * <p>
 * These criteria consist of selectors and patterns which have been specified. With the selectors you can select which
 * files you want to have included. Files which are not selected are excluded. With patterns you can include or exclude
 * files based on their filename.
 * <p>
 * The idea is simple. A given directory is recursively scanned for all files and directories. Each file/directory is
 * matched against a set of selectors, including special support for matching against filenames with include and exclude
 * patterns. Only files/directories which match at least one pattern of the include pattern list or other file selector,
 * and don't match any pattern of the exclude pattern list or fail to match against a required selector will be placed
 * in the list of files/directories found.
 * <p>
 * When no list of include patterns is supplied, "**" will be used, which means that everything will be matched. When no
 * list of exclude patterns is supplied, an empty list is used, such that nothing will be excluded. When no selectors
 * are supplied, none are applied.
 * <p>
 * The filename pattern matching is done as follows: The name to be matched is split up in path segments. A path segment
 * is the name of a directory or file, which is bounded by <code>File.separator</code> ('/' under UNIX, '\' under
 * Windows). For example, "abc/def/ghi/xyz.java" is split up in the segments "abc", "def", "ghi" and "xyz.java".
 * The same is done for the pattern against which should be matched.
 * <p>
 * The segments of the name and the pattern are then matched against each other. When '**' is used for a path segment
 * in the pattern, it matches zero or more path segments of the name.
 * <p>
 * There is a special case regarding the use of <code>File.separator</code>s at the beginning of the pattern and the
 * string to match:<br>
 * When a pattern starts with a <code>File.separator</code>, the string to match must also start with a
 * <code>File.separator</code>. When a pattern does not start with a <code>File.separator</code>, the string to match
 * may not start with a <code>File.separator</code>. When one of these rules is not obeyed, the string will not match.
 * <p>
 * When a name path segment is matched against a pattern path segment, the following special characters can be used:<br>
 * '*' matches zero or more characters<br>
 * '?' matches one character.
 *
 * @author Arnout J. Kuiper <a href="mailto:ajkuiper@wxs.nl">ajkuiper@wxs.nl</a>
 * @author Magesh Umasankar
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @author <a href="mailto:levylambert@tiscali-dsl.de">Antoine Levy-Lambert</a>
 */
public class TrueZipDirectoryScanner
{
    /** Patterns which should be excluded by default. */
    public static final String[] DEFAULTEXCLUDES = {
        "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*", "**/CVS", "**/CVS/**", "**/.cvsignore", "**/SCCS",
        "**/SCCS/**", "**/vssver.scc", "**/.svn", "**/.svn/**", "**/.arch-ids", "**/.arch-ids/**", "**/.bzr",
        "**/.bzr/**", "**/.MySCMServerInfo", "**/.DS_Store" };

    protected TFile basedir;
    protected String[] includes;
    protected String[] excludes;
    protected Vector filesIncluded;
    protected Vector filesNotIncluded;
    protected Vector filesExcluded;
    protected Vector dirsIncluded;
    protected Vector dirsNotIncluded;
    protected Vector dirsExcluded;
    protected Vector filesDeselected;
    protected Vector dirsDeselected;
    protected boolean haveSlowResults = false;
    protected boolean isCaseSensitive = true;
    private boolean followSymlinks = true;
    private boolean followArchive = false;
    protected boolean everythingIncluded = true;

    public TrueZipDirectoryScanner()
    {
    }

    protected static boolean matchPatternStart( String pattern, String str )
    {
        return SelectorUtils.matchPatternStart( pattern, str );
    }

    protected static boolean matchPatternStart( String pattern, String str, boolean isCaseSensitive )
    {
        return SelectorUtils.matchPatternStart( pattern, str, isCaseSensitive );
    }

    protected static boolean matchPath( String pattern, String str )
    {
        return SelectorUtils.matchPath( pattern, str );
    }

    protected static boolean matchPath( String pattern, String str, boolean isCaseSensitive )
    {
        return SelectorUtils.matchPath( pattern, str, isCaseSensitive );
    }

    public static boolean match( String pattern, String str )
    {
        return SelectorUtils.match( pattern, str );
    }

    protected static boolean match( String pattern, String str, boolean isCaseSensitive )
    {
        return SelectorUtils.match( pattern, str, isCaseSensitive );
    }

    public void setBasedir( String basedir )
    {
        setBasedir( new TFile( basedir.replace( '/', TFile.separatorChar ).replace( '\\', TFile.separatorChar ) ) );
    }

    public void setBasedir( TFile basedir )
    {
        this.basedir = basedir;
    }

    public TFile getBasedir()
    {
        return basedir;
    }

    public void setCaseSensitive( boolean isCaseSensitive )
    {
        this.isCaseSensitive = isCaseSensitive;
    }

    public void setFollowSymlinks( boolean followSymlinks )
    {
        this.followSymlinks = followSymlinks;
    }

    /**
     * Returns whether or not inner archives are recursively scanned.
     *
     * @return {@code true} if inner archives are followed
     */
    public boolean isFollowArchive()
    {
        return followArchive;
    }

    /**
     * Sets whether or not inner archives are recursively scanned.
     *
     * @param followArchive {@code true} to follow inner archives
     */
    public void setFollowArchive( boolean followArchive )
    {
        this.followArchive = followArchive;
    }

    public void setIncludes( String[] includes )
    {
        if ( includes == null )
        {
            this.includes = null;
        }
        else
        {
            this.includes = new String[includes.length];
            for ( int i = 0; i < includes.length; i++ )
            {
                String pattern = includes[i].trim().replace( '/', TFile.separatorChar ).replace( '\\', TFile.separatorChar );
                if ( pattern.endsWith( TFile.separator ) )
                {
                    pattern += "**";
                }
                this.includes[i] = pattern;
            }
        }
    }

    public void setExcludes( String[] excludes )
    {
        if ( excludes == null )
        {
            this.excludes = null;
        }
        else
        {
            this.excludes = new String[excludes.length];
            for ( int i = 0; i < excludes.length; i++ )
            {
                String pattern = excludes[i].trim().replace( '/', TFile.separatorChar ).replace( '\\', TFile.separatorChar );
                if ( pattern.endsWith( TFile.separator ) )
                {
                    pattern += "**";
                }
                this.excludes[i] = pattern;
            }
        }
    }

    public boolean isEverythingIncluded()
    {
        return everythingIncluded;
    }

    public void scan()
    {
        if ( basedir == null )
        {
            throw new IllegalStateException( "No basedir set" );
        }
        if ( !basedir.exists() )
        {
            throw new IllegalStateException( "basedir " + basedir + " does not exist" );
        }
        if ( !basedir.isDirectory() )
        {
            throw new IllegalStateException( "basedir " + basedir + " is not a directory" );
        }

        if ( includes == null )
        {
            includes = new String[] { "**" };
        }
        if ( excludes == null )
        {
            excludes = new String[0];
        }

        filesIncluded = new Vector();
        filesNotIncluded = new Vector();
        filesExcluded = new Vector();
        filesDeselected = new Vector();
        dirsIncluded = new Vector();
        dirsNotIncluded = new Vector();
        dirsExcluded = new Vector();
        dirsDeselected = new Vector();

        if ( isIncluded( "" ) )
        {
            if ( !isExcluded( "" ) )
            {
                if ( isSelected( "", basedir ) )
                {
                    dirsIncluded.addElement( "" );
                }
                else
                {
                    dirsDeselected.addElement( "" );
                }
            }
            else
            {
                dirsExcluded.addElement( "" );
            }
        }
        else
        {
            dirsNotIncluded.addElement( "" );
        }
        scandir( basedir, "", true );
    }

    protected void slowScan()
    {
        if ( haveSlowResults )
        {
            return;
        }

        String[] excl = new String[dirsExcluded.size()];
        dirsExcluded.copyInto( excl );
        String[] notIncl = new String[dirsNotIncluded.size()];
        dirsNotIncluded.copyInto( notIncl );

        for ( int i = 0; i < excl.length; i++ )
        {
            if ( !couldHoldIncluded( excl[i] ) )
            {
                scandir( new TFile( basedir, excl[i] ), excl[i] + TFile.separator, false );
            }
        }

        for ( int i = 0; i < notIncl.length; i++ )
        {
            if ( !couldHoldIncluded( notIncl[i] ) )
            {
                scandir( new TFile( basedir, notIncl[i] ), notIncl[i] + TFile.separator, false );
            }
        }

        haveSlowResults = true;
    }

    protected void scandir( TFile dir, String vpath, boolean fast )
    {
        String[] newfiles = dir.list();
        if ( newfiles == null )
        {
            newfiles = new String[0];
        }

        if ( !followSymlinks )
        {
            Vector noLinks = new Vector();
            for ( int i = 0; i < newfiles.length; i++ )
            {
                try
                {
                    if ( isSymbolicLink( dir, newfiles[i] ) )
                    {
                        String name = vpath + newfiles[i];
                        TFile file = new TFile( dir, newfiles[i] );
                        if ( file.isArchive() && this.followArchive )
                        {
                            dirsExcluded.addElement( name );
                        }
                        else if ( file.isDirectory() )
                        {
                            dirsExcluded.addElement( name );
                        }
                        else
                        {
                            filesExcluded.addElement( name );
                        }
                    }
                    else
                    {
                        noLinks.addElement( newfiles[i] );
                    }
                }
                catch ( IOException ioe )
                {
                    System.err.println( "IOException caught while checking for links, couldn't get canonical path!" );
                    noLinks.addElement( newfiles[i] );
                }
            }
            newfiles = new String[noLinks.size()];
            noLinks.copyInto( newfiles );
        }

        for ( int i = 0; i < newfiles.length; i++ )
        {
            String name = vpath + newfiles[i];
            TFile file = new TFile( dir, newfiles[i] );
            if ( ( file.isArchive() && !this.followArchive ) || file.isFile() )
            {
                if ( isIncluded( name ) )
                {
                    if ( !isExcluded( name ) )
                    {
                        if ( isSelected( name, file ) )
                        {
                            filesIncluded.addElement( name );
                        }
                        else
                        {
                            everythingIncluded = false;
                            filesDeselected.addElement( name );
                        }
                    }
                    else
                    {
                        everythingIncluded = false;
                        filesExcluded.addElement( name );
                    }
                }
                else
                {
                    everythingIncluded = false;
                    filesNotIncluded.addElement( name );
                }
            }
            else if ( file.isDirectory() )
            {
                if ( isIncluded( name ) )
                {
                    if ( !isExcluded( name ) )
                    {
                        if ( isSelected( name, file ) )
                        {
                            dirsIncluded.addElement( name );
                            if ( fast )
                            {
                                scandir( file, name + TFile.separator, fast );
                            }
                        }
                        else
                        {
                            everythingIncluded = false;
                            dirsDeselected.addElement( name );
                            if ( fast && couldHoldIncluded( name ) )
                            {
                                scandir( file, name + TFile.separator, fast );
                            }
                        }
                    }
                    else
                    {
                        everythingIncluded = false;
                        dirsExcluded.addElement( name );
                        if ( fast && couldHoldIncluded( name ) )
                        {
                            scandir( file, name + TFile.separator, fast );
                        }
                    }
                }
                else
                {
                    everythingIncluded = false;
                    dirsNotIncluded.addElement( name );
                    if ( fast && couldHoldIncluded( name ) )
                    {
                        scandir( file, name + TFile.separator, fast );
                    }
                }
                if ( !fast )
                {
                    scandir( file, name + TFile.separator, fast );
                }
            }
        }
    }

    protected boolean isIncluded( String name )
    {
        for ( int i = 0; i < includes.length; i++ )
        {
            if ( matchPath( includes[i], name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean couldHoldIncluded( String name )
    {
        for ( int i = 0; i < includes.length; i++ )
        {
            if ( matchPatternStart( includes[i], name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isExcluded( String name )
    {
        for ( int i = 0; i < excludes.length; i++ )
        {
            if ( matchPath( excludes[i], name, isCaseSensitive ) )
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isSelected( String name, TFile file )
    {
        return true;
    }

    public String[] getIncludedFiles()
    {
        String[] files = new String[filesIncluded.size()];
        filesIncluded.copyInto( files );
        return files;
    }

    public String[] getNotIncludedFiles()
    {
        slowScan();
        String[] files = new String[filesNotIncluded.size()];
        filesNotIncluded.copyInto( files );
        return files;
    }

    public String[] getExcludedFiles()
    {
        slowScan();
        String[] files = new String[filesExcluded.size()];
        filesExcluded.copyInto( files );
        return files;
    }

    public String[] getDeselectedFiles()
    {
        slowScan();
        String[] files = new String[filesDeselected.size()];
        filesDeselected.copyInto( files );
        return files;
    }

    public String[] getIncludedDirectories()
    {
        String[] directories = new String[dirsIncluded.size()];
        dirsIncluded.copyInto( directories );
        return directories;
    }

    public String[] getNotIncludedDirectories()
    {
        slowScan();
        String[] directories = new String[dirsNotIncluded.size()];
        dirsNotIncluded.copyInto( directories );
        return directories;
    }

    public String[] getExcludedDirectories()
    {
        slowScan();
        String[] directories = new String[dirsExcluded.size()];
        dirsExcluded.copyInto( directories );
        return directories;
    }

    public String[] getDeselectedDirectories()
    {
        slowScan();
        String[] directories = new String[dirsDeselected.size()];
        dirsDeselected.copyInto( directories );
        return directories;
    }

    public void addDefaultExcludes()
    {
        int excludesLength = excludes == null ? 0 : excludes.length;
        String[] newExcludes = new String[excludesLength + DEFAULTEXCLUDES.length];
        if ( excludesLength > 0 )
        {
            System.arraycopy( excludes, 0, newExcludes, 0, excludesLength );
        }
        for ( int i = 0; i < DEFAULTEXCLUDES.length; i++ )
        {
            newExcludes[i + excludesLength] =
                DEFAULTEXCLUDES[i].replace( '/', TFile.separatorChar ).replace( '\\', TFile.separatorChar );
        }
        excludes = newExcludes;
    }

    /**
     * Checks whether a given file is a symbolic link.
     *
     * @param parent the parent directory of the file to test
     * @param name the name of the file to test
     * @return {@code true} if the file is a symbolic link
     * @throws IOException if the canonical path cannot be resolved
     */
    public boolean isSymbolicLink( TFile parent, String name )
        throws IOException
    {
        TFile resolvedParent = new TFile( parent.getCanonicalPath() );
        TFile toTest = new TFile( resolvedParent, name );
        return !toTest.getAbsolutePath().equals( toTest.getCanonicalPath() );
    }
}
