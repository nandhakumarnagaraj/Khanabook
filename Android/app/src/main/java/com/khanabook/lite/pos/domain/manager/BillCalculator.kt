package com.khanabook.lite.pos.domain.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import java.math.BigDecimal
import java.math.RoundingMode

object BillCalculator {

    data class GstBreakdown(
        val cgst: Double,
        val sgst: Double,
        val totalGst: Double
    )

    fun calculateSubtotal(items: List<Pair<Double, Int>>): Double {
        var subtotal = BigDecimal.ZERO
        items.forEach { (price, qty) ->
            val itemTotal = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(qty.toLong()))
            subtotal = subtotal.add(itemTotal)
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    fun calculateGST(subtotal: Double, gstPct: Double, isInclusive: Boolean = false): GstBreakdown {
        val bdSubtotal = BigDecimal.valueOf(subtotal)
        val bdGstPct = BigDecimal.valueOf(gstPct)
        val bd100 = BigDecimal.valueOf(100)
        
        val totalGst = if (isInclusive) {
            // Formula: Tax = Amount - (Amount / (1 + Rate/100))
            val multiplier = BigDecimal.ONE.add(bdGstPct.divide(bd100, 4, RoundingMode.HALF_UP))
            val baseAmount = bdSubtotal.divide(multiplier, 2, RoundingMode.HALF_UP)
            bdSubtotal.subtract(baseAmount)
        } else {
            // Formula: Tax = Amount * (Rate/100)
            bdSubtotal.multiply(bdGstPct).divide(bd100, 2, RoundingMode.HALF_UP)
        }
        
        val cgst = totalGst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
        val sgst = totalGst.subtract(cgst)
        
        return GstBreakdown(cgst.toDouble(), sgst.toDouble(), totalGst.toDouble())
    }

    fun calculateCustomTax(subtotal: Double, taxPct: Double): Double {
        val bdSubtotal = BigDecimal.valueOf(subtotal)
        val bdTaxPct = BigDecimal.valueOf(taxPct)
        
        return bdSubtotal.multiply(bdTaxPct)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun calculateTotal(
        subtotal: Double,
        cgst: Double,
        sgst: Double,
        customTax: Double,
        isInclusive: Boolean = false
    ): Double {
        return if (isInclusive) {
            // Total is already equal to subtotal in inclusive mode
            BigDecimal.valueOf(subtotal).setScale(2, RoundingMode.HALF_UP).toDouble()
        } else {
            val total = BigDecimal.valueOf(subtotal)
                .add(BigDecimal.valueOf(cgst))
                .add(BigDecimal.valueOf(sgst))
                .add(BigDecimal.valueOf(customTax))
            total.setScale(2, RoundingMode.HALF_UP).toDouble()
        }
    }

    fun validatePartPayment(a1: Double, a2: Double, total: Double): Boolean {
        val sum = BigDecimal.valueOf(a1).add(BigDecimal.valueOf(a2))
            .setScale(2, RoundingMode.HALF_UP)
        val bdTotal = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP)
        
        return sum.compareTo(bdTotal) == 0
    }
}


