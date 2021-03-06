package com.example.questlogalpha

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Solves for a real root x in ax^3 + bx^2 + cx + d = toSolveFor.
 * @param a
 * @param b
 * @param c
 * @param d // optional
 * @param toSolveFor // optional
 * @return the one real root. If there are no real roots, returns -1.0.
 * */

fun cubicEquation(a:Double, b:Double, c:Double, d:Double=0.0, toSolveFor:Double = 0.0) : Double
{
    Log.d(Util.TAG, "cubicEquation: a: $a, b: $b, c: $c, d: $d, toSolveFor: $toSolveFor")

    if(d < 0) {
        Log.e(Util.TAG, "cubicEquation: this eq only has complex roots. This function is too simple for that.")
        return -1.0
    }
    // AX³+BX²+CX+D = toSolveFor

    // balance eq to 0
    var nD = d
    if(toSolveFor > 0.0)
    {
        nD = d - toSolveFor
    }

    val p = (-b / (3*a))
    val q = p.pow(3) + (b*c - (3*a*nD)) / (6*(a.pow(2)))
    val r = (c/(3*a))

    //{q + [q2 + (r-p2)3]1/2}1/3   +   {q - [q2 + (r-p2)3]1/2}1/3   +   p
    val thing = sqrt(q.pow(2) + (r - p.pow(2)).pow(3))
    val total = Math.cbrt(q + thing) + Math.cbrt(q-thing) + p

    Log.d(Util.TAG, "cubicEquation: total: $total")

    return total
}

// same as the one above, just breaking it apart differently I guess
fun cubicEquation2(a:Double, b:Double, c:Double, d:Double=0.0, toSolveFor:Double = 0.0) : Double {
    var nD = d
    if(toSolveFor > 0.0)
    {
        nD = d - toSolveFor
    }
    val first =  ((-b).pow(3) / (27*(a.pow(3))))
    val second = ((b*c) / (6*(a.pow(2))))
    val third = (nD/(2*a))

    val firstParen = (first + second - third)
    val secondParen = firstParen.pow(2)
    val thirdParen = ((c / (3*a)) - (b.pow(2) / (9*(a.pow(2))))).pow(3)
    val firstUnderSquare = sqrt(secondParen + thirdParen)


    val firstHalf = Math.cbrt(firstParen + firstUnderSquare)
    val secondHalf = Math.cbrt(firstParen - firstUnderSquare)

    val total = ((firstHalf + secondHalf) - (b / (3*a)))


    Log.d(Util.TAG, "cubicEquation2: total: $total")
    return total
}

fun round(value: Double, places: Int): Double {
    require(places >= 0)

    var bd = BigDecimal.valueOf(value)
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}

// todo add geometry formula

class Util {

    companion object {

        /** Show a short toast */
        fun showShortToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }

        /** Show a long toast */
        fun showLongToast(context: Context, text: String) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

        /**  Converts a [dp] value into its equivalent in px */
        fun dpToPixels(dp: Float, resources: Resources) : Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.displayMetrics
            ).toInt()
        }

        /**  Converts a [drawable] to a bitmap */
        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                if (drawable.bitmap != null) {
                    return drawable.bitmap
                }
            }
            val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
            }
            else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
            drawable.draw(canvas)
            return bitmap
        }

        /** animate a strikethough on a text [view]. */
        fun styleComplete(view: TextView){
            animateStrikeThrough1(view)
            // view.paintFlags = (view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
            view.setTextColor(ContextCompat.getColor(view.context, R.color.colorTertiary))
        }

        private fun animateStrikeThrough1(tv: TextView) {
            val ANIM_DURATION:Long = 250 //duration of animation in millis
            val length = tv.text.length
            object: CountDownTimer(ANIM_DURATION, ANIM_DURATION/length) {

                val span: Spannable = SpannableString(tv.text)
                val strikethroughSpan: StrikethroughSpan = StrikethroughSpan()

                override fun onTick(millisUntilFinished:Long){
                    //calculate end position of strikethrough in textview
                    var endPosition:Int = (((millisUntilFinished-ANIM_DURATION)*-1)/(ANIM_DURATION/length)).toInt() + 1
                    if(endPosition > length) endPosition = length
                    span.setSpan(strikethroughSpan, 0, endPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    tv.text = span
                }

                override fun onFinish(){}
            }.start()
        }

        // -------------------------- log tag ------------------------------ //
        const val TAG: String = "KSLOG: Util.kt"
    }
}

fun Any.toast(context: Context, duration: Int = Toast.LENGTH_SHORT): Toast {
    return Toast.makeText(context, this.toString(), duration).apply { show() }
}
