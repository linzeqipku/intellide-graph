package cn.edu.pku.sei.intellide.graph.extraction.mail.utils;

/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

interface FromLinePatterns {

    /**
     * Match a line like: From ieugen@apache.org Fri Sep 09 14:04:52 2011
     */
    String DEFAULT = "^From \\S+@\\S.*\\d{4}$";
    /**
     * Matches other type of From_ line (without @):
     * From MAILER-DAEMON Wed Oct 05 21:54:09 2011
     * Thunderbird mbox content: From - Wed Apr 02 06:51:08 2014
     */
    String DEFAULT2 = "^From \\S+.*\\d{4}$";


}