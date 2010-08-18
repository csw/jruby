/*
 **** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2010 Charles Oliver Nutter <headius@headius.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jruby.ext.jruby;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyBoolean;
import org.jruby.RubyModule;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.runtime.load.Library;

/**
 * Utilities library for all those methods that don't need the full 'java' library
 * to be loaded. This is done mostly for performance reasons. For example, for those
 * who only need to enable the object space, not loading 'java' might save 200-300ms
 * of startup time, like in case of jirb.
 */
public class JRubyUtilLibrary implements Library {

    public void load(Ruby runtime, boolean wrap) throws IOException {
        RubyModule mJRubyUtil = runtime.getOrCreateModule("JRuby").defineModuleUnder("Util");
        mJRubyUtil.defineAnnotatedMethods(JRubyUtilLibrary.class);
    }

    @JRubyMethod(module = true)
    public static void gc(IRubyObject recv) {
        System.gc();
    }

    @JRubyMethod(name = "objectspace", module = true)
    public static IRubyObject getObjectSpaceEnabled(IRubyObject recv) {
        Ruby runtime = recv.getRuntime();
        return RubyBoolean.newBoolean(runtime, runtime.isObjectSpaceEnabled());
    }

    @JRubyMethod(name = "objectspace=", module = true)
    public static IRubyObject setObjectSpaceEnabled(IRubyObject recv, IRubyObject arg) {
        Ruby runtime = recv.getRuntime();
        runtime.setObjectSpaceEnabled(arg.isTrue());
        return runtime.getNil();
    }

    @SuppressWarnings(value = "deprecation")
    @JRubyMethod(name = "classloader_resources", module = true)
    public static IRubyObject getClassLoaderResources(IRubyObject recv, IRubyObject arg) {
        Ruby runtime = recv.getRuntime();
        String resource = arg.convertToString().toString();
        final List<RubyString> urlStrings = new ArrayList<RubyString>();
        try {
            Enumeration<URL> urls = runtime.getJRubyClassLoader().getResources(resource);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String urlString;
                if ("jar".equals(url.getProtocol()) && url.getFile().startsWith("file:/")) {
                    try {
                        urlString = url.toURI().getSchemeSpecificPart();
                    } catch (java.net.URISyntaxException urise) {
                        if (runtime.getInstanceConfig().isDebug()) {
                            runtime.getErr().println("URISyntaxException trying to parse " + url + ", stack trace follows:");
                            urise.printStackTrace(runtime.getErr());
                        }
                        urlString = null;
                    }
                } else {
                    urlString = url.getFile();
                }
                urlStrings.add(runtime.newString(urlString));
            }
            return RubyArray.newArrayNoCopy(runtime, urlStrings.toArray(new IRubyObject[urlStrings.size()]));
        } catch (IOException ignore) {
        }
        return runtime.newEmptyArray();
    }
}