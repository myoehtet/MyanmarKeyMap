package com.example.myanmarkeymap

// Classic Myanmar3 reference mapping; US QWERTY physical positions.
object MyanmarLayout {
    private const val keys = "`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./"
    private const val normal = "ၐ၁၂၃၄၅၆၇၈၉၀-=ဆတနမအပကငသစဟဩ၏ေျိ်ါ့ြုူး'ဖထခလဘညာ,./"
    private const val shifted = "ဎဍၒဋၓၔၕရဂ()ဵ+ဈဿဣ၎ဤ၌ဥ၍ဝဏဧဪၑဗှီ္ွံဲဒဓ:\"ဇဌဃဠယဉဦ၊။?"
    init { require(keys.length == normal.length && keys.length == shifted.length) }
    fun map(key: Char, shift: Boolean = false): Char? = keys.indexOf(key).takeIf { it >= 0 }?.let { if (shift) shifted[it] else normal[it] }
}
