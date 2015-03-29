/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.io.json;

import rapaio.io.json.stream.JsonSpliterator;
import rapaio.io.json.tree.JsonValue;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/20/15.
 */
public final class Json {

    public static JsonStream stream(File root, FileFilter ff) {
        return new JsonStream(Json.stream(root, ff, msg -> {
        }, Json.allFilter()));
    }

    public static JsonStream stream(File root, FileFilter ff, Function<String, Boolean> propFilter) {
        return new JsonStream(Json.stream(root, ff, msg -> {
        }, propFilter));
    }

    public static JsonStream stream(File root, FileFilter ff, Consumer<String> ph, Function<String, Boolean> propFilter) {
        List<File> files = new ArrayList<>();
        if (root.isDirectory()) {
            files = Arrays.asList(root.listFiles()).stream().filter(ff::accept).collect(Collectors.toList());
        } else {
            files.add(root);
        }
        JsonSpliterator spliterator = new JsonSpliterator(files, ph, propFilter);
        return new JsonStream(StreamSupport.stream(spliterator, spliterator.isParallel()));
    }

    public static void write(OutputStream os, JsonValue js) throws IOException {
        Writer w = new OutputStreamWriter(os);
        w.append(js.toString()).append('\n');
        w.flush();
    }

    public static Function<String, Boolean> allFilter() {
        return key -> true;
    }

    public static Function<String, Boolean> inFilter(String... keys) {
        final HashSet<String> allow = new HashSet<>();
        Collections.addAll(allow, keys);
        return key -> allow.isEmpty() || allow.contains(key);
    }
}