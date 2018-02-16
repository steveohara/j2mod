/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghgande.j2mod.modbus.msg;

import com.ghgande.j2mod.modbus.Modbus;

/**
 * @author jfhaugh
 * @author Steve O'Hara (4energy)
 * @version 2.0 (March 2016)
 */
public class IllegalFunctionExceptionResponse extends ExceptionResponse {

    /**
     *
     */
    public IllegalFunctionExceptionResponse() {
        super(0, Modbus.ILLEGAL_FUNCTION_EXCEPTION);
    }

    public IllegalFunctionExceptionResponse(int function) {
        super(function | Modbus.EXCEPTION_OFFSET, Modbus.ILLEGAL_FUNCTION_EXCEPTION);
    }

    /**
     *
     * @param fc
     */
    public void setFunctionCode(int fc) {
        super.setFunctionCode(fc | Modbus.EXCEPTION_OFFSET);
    }
}
