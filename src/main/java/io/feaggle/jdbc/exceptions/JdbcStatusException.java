/* Copyright (c) 2019-present, Kevin Mas Ruiz
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.feaggle.jdbc.exceptions;

public class JdbcStatusException extends RuntimeException {
    public JdbcStatusException(String message) {
        super(message);
    }

    public JdbcStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
