package org.codehaus.mojo.truezip;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.mojo.truezip.internal.DefaultTrueZip;
import org.codehaus.mojo.truezip.internal.DefaultTrueZipArchiveDetector;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.FsSyncException;

public abstract class AbstractArchiveMojo
    extends AbstractMojo
{

    /**
     * Maven project.
     * Since 1.0 beta-1.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * Since 1.0 beta-1.
     */
    protected TrueZip truezip = new DefaultTrueZip();

    /**
     * Enable automatic file update after each MOJO execution. If set to <code>false</code>, immediate update is not
     * performed. Then, the updated files are flushed at undefined time (when the VM finalizes objects). Otherwise, a
     * forced file update can be triggered by using <code>update</code> goal in an separate execution.
     *
     * Default is true.
     * Since 1.0 beta-2.
     */
    @Parameter(defaultValue = "true")
    protected boolean immediateUpdate;

    /**
     * Skip execution.
     * Since 1.2.
     */
    @Parameter(defaultValue = "false")
    protected boolean skip;

    protected String resolveRelativePath( String path )
    {
        if ( path != null && !new TFile( path ).isAbsolute() )
        {
            path = new TFile( this.project.getBasedir(), path ).getAbsolutePath();
        }

        return path;
    }

    protected void resolveRelativePath( FileSet fileSet )
    {
        fileSet.setDirectory( resolveRelativePath( fileSet.getDirectory() ) );
        fileSet.setOutputDirectory( resolveRelativePath( fileSet.getOutputDirectory() ) );
    }

    protected void resolveRelativePath( FileItem fileItem )
    {
        fileItem.setOutputDirectory( resolveRelativePath( fileItem.getOutputDirectory() ) );
        fileItem.setSource( resolveRelativePath( fileItem.getSource() ) );
    }

    protected void tryImmediateUpdate()
        throws MojoExecutionException
    {
        if ( immediateUpdate )
        {
            try
            {
                truezip.sync();
            }
            catch ( FsSyncException e )
            {
                throw new MojoExecutionException( "Immediate file update failed!", e );
            }
        }
    }

    protected void initializeArchiveDetector()
    {
        DefaultTrueZipArchiveDetector archiveDetector = new DefaultTrueZipArchiveDetector();
        archiveDetector.init();
    }

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        // StaticLoggerBinder.getSingleton().setMavenLog( this.getLog() );
    }
}
