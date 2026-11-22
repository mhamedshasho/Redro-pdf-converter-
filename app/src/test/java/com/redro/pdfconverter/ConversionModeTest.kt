package com.redro.pdfconverter

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversionModeTest {
    @Test
    fun exposesAllSupportedConversionModes() {
        assertEquals(
            listOf("EDITABLE", "VISUAL", "HYBRID"),
            ConversionMode.entries.map { it.name }
        )
    }
}
