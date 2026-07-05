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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import de.schlichtherle.truezip.fs.FsSyncException;

/**
 * Update open archives immediately, flush cached data to disk.
 *
 * Deprecated: use immediateUpdate option instead.
 * Goal: update
 * Phase: process-resources
 * Version: $Id: $
 */
@Mojo(name = "update", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ForceFileUpdateMojo
    extends AbstractArchiveMojo
{

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            this.getLog().info( "Skip this execution" );
            return;
        }

        super.execute();

        try
        {
            truezip.sync();
        }
        catch ( FsSyncException e )
        {
            throw new MojoExecutionException( "Forced file update failed!", e );
        }
    }

}
