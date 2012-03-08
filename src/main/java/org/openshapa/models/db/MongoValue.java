/*
 * Copyright (c) 2011 OpenSHAPA Foundation, http://openshapa.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.openshapa.models.db;

import com.mongodb.BasicDBObject;
import java.io.Serializable;


public abstract class MongoValue extends BasicDBObject implements Value, Serializable {
    
     String value;
    
     /**
     * @param value The string to test if it is valid.
     *
     * @return True if the supplied value is a valid substitute 
     */
    public boolean isValid(final String value) {
        return true;
    } 

    /**
     * Clears the contents of the value and returns it to a 'null'/Empty state.
     */
    public void clear() {
        value = null;
    }

    /**
     * @return True if the value is empty/'null' false otherwise.
     */
    public boolean isEmpty() {
        if(value == null) {
            return true;
        } else {
            return false;
        }
    }
    
    public abstract void save();

    /**
     * Sets the value, this method leaves the value unchanged if the supplied
     * input is invalid. Use isValid to test.
     *
     * @param value The new content to use for this value.
     */
    public abstract void set(final String value);

    /**
     * @return must override toString in such a way that when isEmpty == true,
     * toString returns a valid empty value i.e. "<argName>"
     */
    
    public abstract String toString();
}