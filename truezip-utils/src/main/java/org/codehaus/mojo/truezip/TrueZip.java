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

import java.io.IOException;
import java.util.List;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.fs.FsSyncException;

/**
 * Generic interface to manipulate recognizable archives.
 */
public interface TrueZip
{
    /**
     * List the files from the {@link TrueZipFileSet} configuration.
     *
     * @param fileSet the file set configuration
     * @return list of {@link TFile} instances
     */
    List<TFile> list( TrueZipFileSet fileSet );

    /**
     * Copy a set of files to another archive using the file set configuration.
     *
     * @param oneFileSet the file set configuration
     * @throws IOException if an I/O error occurs
     */
    void copy( TrueZipFileSet oneFileSet )
        throws IOException;

    /**
     * Copy a file or archive to another destination. Hash values of source sub-archives are not kept intact during
     * copy. Use the {@link #copy(TrueZipFileSet)} method instead if needed.
     *
     * @param source the source file
     * @param dest the destination file
     * @throws IOException if an I/O error occurs
     */
    void copyFile( TFile source, TFile dest )
        throws IOException;

    /**
     * Move a file.
     *
     * @param source the source file
     * @param dest the destination file
     * @throws IOException if an I/O error occurs
     */
    void moveFile( TFile source, TFile dest )
        throws IOException;

    /**
     * Move a set of files from one archive to another.
     *
     * @param oneFileSet the archive setup
     * @throws IOException if an I/O error occurs
     */
    void move( TrueZipFileSet oneFileSet )
        throws IOException;

    /**
     * Remove a set of files from the archive setup.
     *
     * @param oneFileSet the archive setup
     * @throws IOException if an I/O error occurs
     */
    void remove( TrueZipFileSet oneFileSet )
        throws IOException;

    /**
     * Perform a global sync.
     *
     * @throws FsSyncException if synchronization fails
     */
    void sync()
        throws FsSyncException;

    /**
     * Perform a selective sync.
     *
     * @param file the file to sync
     * @throws FsSyncException if synchronization fails
     */
    void sync( TFile file )
        throws FsSyncException;

}
