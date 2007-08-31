/*
 * Simple Java Dict Client (RFC2229)
 *
 * $Id: Match.java,v 1.4 2006/03/06 12:41:32 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.metaparadigm.dict;

import java.io.Serializable;

public class Match implements Serializable {

    private final static long serialVersionUID = 2;

    private String word;
    private String database;

    public String getDatabase() {
        return database;
    }

    public String getWord() {
        return word;
    }

    public Match(String database, String word) {
        this.database = database;
        this.word = word;
    }

    public String toString() {
        return "match: " + word + " \"" + database + "\"";
    }

}
