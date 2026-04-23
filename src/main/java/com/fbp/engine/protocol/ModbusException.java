/*
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * + Copyright 2026. NHN Academy Corp. All rights reserved.
 * + * While every precaution has been taken in the preparation of this resource,  assumes no
 * + responsibility for errors or omissions, or for damages resulting from the use of the information
 * + contained herein
 * + No part of this resource may be reproduced, stored in a retrieval system, or transmitted, in any
 * + form or by any means, electronic, mechanical, photocopying, recording, or otherwise, without the
 * + prior written permission.
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 */

package com.fbp.engine.protocol;

import lombok.Getter;

public class ModbusException extends Exception {

    private final int functionCode;

    @Getter
    private final int exceptionCode;

    private static final int ILLEGAL_FUNCTION = 0x01;

    private static final int ILLEGAL_DATA_ADDRESS = 0x02;

    private static final int ILLEGAL_DATA_VALUE = 0x03;

    private static final int SLAVE_DEVICE_FAILURE = 0x04;

    public ModbusException(int functionCode, int exceptionCode) {
        this.functionCode = functionCode;
        this.exceptionCode = exceptionCode;
    }

    @Override
    public String getMessage() {
        if (exceptionCode == ILLEGAL_FUNCTION) {
            return "MODBUS 에러 — FC: 0x%02X, Exception: 0x%02X (%s)"
                    .formatted(functionCode, exceptionCode, "Illegal Function — 지원하지 않는 Function Code");
        } else if (exceptionCode == ILLEGAL_DATA_ADDRESS) {
            return "MODBUS 에러 — FC: 0x%02X, Exception: 0x%02X (%s)"
                    .formatted(functionCode, exceptionCode, "Illegal Data Address — 존재하지 않는 레지스터 주소");
        } else if (exceptionCode == ILLEGAL_DATA_VALUE) {
            return "MODBUS 에러 — FC: 0x%02X, Exception: 0x%02X (%s)"
                    .formatted(functionCode, exceptionCode, "Illegal Data Value — 값이 허용 범위를 벗어남");
        } else if (exceptionCode == SLAVE_DEVICE_FAILURE) {
            return "MODBUS 에러 — FC: 0x%02X, Exception: 0x%02X (%s)"
                    .formatted(functionCode, exceptionCode, "Slave Device Failure — 장비 내부 오류");
        } else {
            return "MODBUS 에러 — FC: 0x%02X, Exception: 0x%02X (%s)"
                    .formatted(functionCode, exceptionCode, "예상치 못한 오류");
        }
    }

}
