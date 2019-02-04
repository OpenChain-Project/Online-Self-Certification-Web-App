/**
 * Copyright (c) 2019 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
/**
 * Contains code from the enhanced-resources project under the MIT license:
 * 
 * Copyright (c) 2016-2017 John O'Conner
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.openchain.certification;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * Class to utilize UTF8 encoding for string resources
 * Code originates from the Enhanced Resources project: https://github.com/joconner/enhanced-resources/
 * @author Gary O'Neall
 * 
 *
 */
public class Utf8ResourceBundleControl extends Control {
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format,
            ClassLoader loader, boolean reload)  throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        ResourceBundle bundle = null;
        if (format.equals("java.class")) {
            bundle = super.newBundle(baseName, locale, format, loader, reload);
        } else if (format.equals("java.properties")) {
            final String resourceName = bundleName.contains("://") ? null :
                    toResourceName(bundleName, "properties");
            if (resourceName == null) {
                return bundle;
            }
            final ClassLoader classLoader = loader;
            InputStream stream = null;
            if (reload) {
                stream = reload(resourceName, classLoader);
            } else {
                stream = classLoader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                Reader reader = new InputStreamReader(stream, "UTF-8");
                try {
                    bundle = new PropertyResourceBundle(reader);
                } finally {
                    reader.close();
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown format: " + format);
        }
        return bundle;
	}
	
    InputStream reload(String resourceName, ClassLoader classLoader) throws IOException {
        InputStream stream = null;
        URL url = classLoader.getResource(resourceName);
        if (url != null) {
            URLConnection connection = url.openConnection();
            if (connection != null) {
                // Disable caches to get fresh data for
                // reloading.
                connection.setUseCaches(false);
                stream = connection.getInputStream();
            }
        }
        return stream;
    }

}
