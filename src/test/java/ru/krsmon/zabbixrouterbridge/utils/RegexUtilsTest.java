package ru.krsmon.zabbixrouterbridge.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class RegexUtilsTest {

    @Test
    void toArpMap_withoutThrowException() {
        var table = "00:11:22:33:44:55 123.123.123.123 \n 00:11:22:33:44:55 321.321.321.321";
        RegexUtils.toArpMap(table);
    }
}