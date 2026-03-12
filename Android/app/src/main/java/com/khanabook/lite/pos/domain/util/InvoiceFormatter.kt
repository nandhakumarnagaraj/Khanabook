package com.khanabook.lite.pos.domain.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import com.khanabook.lite.pos.data.local.relation.BillWithItems
import com.khanabook.lite.pos.data.local.entity.RestaurantProfileEntity

object InvoiceFormatter {

    // —— ESC/POS Thermal Printer Commands ——————————————————————————————————————
    private val ESC: Byte = 0x1B
    private val GS: Byte  = 0x1D
    private val RESET      = byteArrayOf(ESC, 0x40) // Initialize
    private val BOLD_ON    = byteArrayOf(ESC, 0x45, 0x01)
    private val BOLD_OFF   = byteArrayOf(ESC, 0x45, 0x00)
    private val ALIGN_LEFT  = byteArrayOf(ESC, 0x61, 0x00)
    private val ALIGN_CENTER= byteArrayOf(ESC, 0x61, 0x01)
    private val LARGE_FONT = byteArrayOf(GS, 0x21, 0x11) // 2x height, 2x width
    private val NORMAL_FONT= byteArrayOf(GS, 0x21, 0x00)
    private val CUT_PAPER  = byteArrayOf(GS, 0x56, 0x42, 0x00) // Cut command

    fun formatForThermalPrinter(bill: BillWithItems, profile: RestaurantProfileEntity?): ByteArray {
        val charsPerLine = if (profile?.paperSize == "80mm") 42 else 32
        val currency = if (profile?.currency == "INR" || profile?.currency == "Rupee") "\u20b9" else profile?.currency ?: ""
        val isGst = profile?.gstEnabled == true
        
        val width = charsPerLine
        val line = "-".repeat(width)
        val doubleLine = "=".repeat(width)

        val out = mutableListOf<Byte>()

        fun add(bytes: ByteArray) { out.addAll(bytes.toList()) }
        fun add(text: String) { out.addAll(text.toByteArray(Charsets.US_ASCII).toList()) }

        add(RESET)
        add(ALIGN_CENTER)
        
        // Shop Name Large Bold
        add(LARGE_FONT)
        add(BOLD_ON)
        add(profile?.shopName?.uppercase() ?: "RESTAURANT")
        add("\n")
        add(BOLD_OFF)
        add(NORMAL_FONT)
        
        // Address & Info
        profile?.shopAddress?.takeIf { it.isNotBlank() }?.let { add(it + "\n") }
        if (!profile?.whatsappNumber.isNullOrBlank()) add("Contact: ${profile?.whatsappNumber}\n")
        if (isGst && !profile?.gstin.isNullOrBlank()) add("GSTIN: ${profile?.gstin}\n")
        
        add(ALIGN_LEFT)
        add("$doubleLine\n")
        add(centerText(if (isGst) "TAX INVOICE" else "INVOICE", width) + "\n")
        add("$line\n")
        
        add("Daily Order : ${bill.bill.dailyOrderDisplay}\n")
        add("Order #     : ${bill.bill.lifetimeOrderId.toString().padStart(5, '0')}\n")
        add("Date        : ${bill.bill.createdAt}\n")
        bill.bill.customerName?.takeIf { it.isNotBlank() }?.let { add("Customer    : $it\n") }
        
        add("$line\n")
        
        // Dynamic Column Widths
        val itemW = (width * 0.45).toInt()
        val qtyW = 4
        val rateW = (width * 0.2).toInt()
        val amtW = width - itemW - qtyW - rateW - 3
        
        val headerFormat = "%-${itemW}s %${qtyW}s %${rateW}s %${amtW}s\n"
        add(String.format(headerFormat, "ITEM", "QTY", "RATE", "AMT"))
        add("$line\n")
        
        for (item in bill.items) {
            val name = if (item.variantName != null) "${item.itemName} (${item.variantName})" else item.itemName
            add(String.format(headerFormat, name.take(itemW), item.quantity, "%.0f".format(item.price), "%.0f".format(item.itemTotal)))
        }
        
        add("$line\n")
        
        // Summary
        add(formatRow("Subtotal:", "$currency ${"%.2f".format(bill.bill.subtotal)}", width))
        
        if (isGst && bill.bill.cgstAmount > 0) {
            val halfGst = bill.bill.gstPercentage / 2
            add(formatRow("CGST (%.1f%%):".format(halfGst), "$currency ${"%.2f".format(bill.bill.cgstAmount)}", width))
            add(formatRow("SGST (%.1f%%):".format(halfGst), "$currency ${"%.2f".format(bill.bill.sgstAmount)}", width))
        }
        
        if (!isGst && bill.bill.customTaxAmount > 0) {
            add(formatRow("Tax:", "$currency ${"%.2f".format(bill.bill.customTaxAmount)}", width))
        }
        
        add("$line\n")
        add(BOLD_ON)
        add(formatRow("TOTAL AMOUNT:", "$currency ${"%.2f".format(bill.bill.totalAmount)}", width))
        add(BOLD_OFF)
        add("$line\n")
        
        add("Payment Mode : ${bill.bill.paymentMode.uppercase()}\n")
        
        add("$doubleLine\n")
        add(ALIGN_CENTER)
        add("Thank you for visiting us!\n")
        add("Have a great day!\n")
        add("$doubleLine\n")
        
        // Feed & Cut
        add("\n\n\n\n")
        add(CUT_PAPER)

        return out.toByteArray()
    }

    fun formatForWhatsApp(bill: BillWithItems, profile: RestaurantProfileEntity?): String {
        return formatForPrinter(bill, profile, 32) // Default to 32 chars for digital
    }

    fun formatForPrinter(bill: BillWithItems, profile: RestaurantProfileEntity?, charsPerLine: Int = 32): String {
        val sb = StringBuilder()
        val currency = if (profile?.currency == "INR" || profile?.currency == "Rupee") "\u20b9" else profile?.currency ?: ""
        val isGst = profile?.gstEnabled == true
        
        val width = charsPerLine
        val line = "-".repeat(width)
        val doubleLine = "=".repeat(width)

        // Header
        sb.append("$doubleLine\n")
        sb.append(centerText(profile?.shopName?.uppercase() ?: "RESTAURANT", width) + "\n")
        profile?.shopAddress?.takeIf { it.isNotBlank() }?.let { address ->
            sb.append(centerText(address, width) + "\n")
        }
        if (!profile?.whatsappNumber.isNullOrBlank()) sb.append(centerText("Contact: ${profile?.whatsappNumber}", width) + "\n")
        
        if (isGst && !profile?.gstin.isNullOrBlank()) {
            sb.append(centerText("GSTIN: ${profile?.gstin}", width) + "\n")
        }
        
        sb.append("$doubleLine\n")
        sb.append(centerText(if (isGst) "TAX INVOICE" else "INVOICE", width) + "\n")
        sb.append("$line\n")
        
        sb.append("Daily Order : ${bill.bill.dailyOrderDisplay}\n")
        sb.append("Order #     : ${bill.bill.lifetimeOrderId.toString().padStart(5, '0')}\n")
        sb.append("Date        : ${bill.bill.createdAt}\n")
        bill.bill.customerName?.takeIf { it.isNotBlank() }?.let { sb.append("Customer    : $it\n") }
        
        sb.append("$line\n")
        
        // Dynamic Column Widths
        // ITEM(Width-17) QTY(3) RATE(7) AMT(7) -> 34 total? No, lets scale.
        val itemW = (width * 0.45).toInt()
        val qtyW = 4
        val rateW = (width * 0.2).toInt()
        val amtW = width - itemW - qtyW - rateW - 3
        
        val headerFormat = "%-${itemW}s %${qtyW}s %${rateW}s %${amtW}s\n"
        sb.append(String.format(headerFormat, "ITEM", "QTY", "RATE", "AMT"))
        sb.append("$line\n")
        
        for (item in bill.items) {
            val name = if (item.variantName != null) "${item.itemName} (${item.variantName})" else item.itemName
            sb.append(String.format(headerFormat, name.take(itemW), item.quantity, "%.0f".format(item.price), "%.0f".format(item.itemTotal)))
        }
        
        sb.append("$line\n")
        
        // Summary
        sb.append(formatRow("Subtotal:", "$currency ${"%.2f".format(bill.bill.subtotal)}", width))
        
        if (isGst && bill.bill.cgstAmount > 0) {
            val halfGst = bill.bill.gstPercentage / 2
            sb.append(formatRow("CGST (%.1f%%):".format(halfGst), "$currency ${"%.2f".format(bill.bill.cgstAmount)}", width))
            sb.append(formatRow("SGST (%.1f%%):".format(halfGst), "$currency ${"%.2f".format(bill.bill.sgstAmount)}", width))
        }
        
        if (!isGst && bill.bill.customTaxAmount > 0) {
            sb.append(formatRow("Tax:", "$currency ${"%.2f".format(bill.bill.customTaxAmount)}", width))
        }
        
        sb.append("$line\n")
        sb.append(formatRow("TOTAL AMOUNT:", "$currency ${"%.2f".format(bill.bill.totalAmount)}", width))
        sb.append("$line\n")
        
        sb.append("Payment Mode : ${bill.bill.paymentMode.uppercase()}\n")
        
        sb.append("$doubleLine\n")
        sb.append(centerText("Thank you for visiting us!", width) + "\n")
        sb.append(centerText("Have a great day!", width) + "\n")
        sb.append("$doubleLine\n")
        
        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = kotlin.math.max(0, (width - text.length) / 2)
        return " ".repeat(padding) + text
    }

    private fun formatRow(label: String, value: String, width: Int): String {
        val spaceCount = width - label.length - value.length
        return if (spaceCount > 0) label + " ".repeat(spaceCount) + value + "\n"
        else "$label\n${" ".repeat(width - value.length)}$value\n"
    }
}


