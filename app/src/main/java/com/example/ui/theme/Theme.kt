package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// 🎨 Colors inspired by your logo
val SaffronOrange = Color(0xFFFF7A00)
val GoldenOrange = Color(0xFFFFB347)
val DeepOrange = Color(0xFFFF9933)
val OffWhite = Color(0xFFFAF9F6)

// 🌙 Dark theme palette
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)

private val isTesting: Boolean by lazy {
    try {
        Class.forName("org.junit.Test") != null
    } catch (e: Exception) {
        false
    }
}

// 🖋 Fonts (lazily loaded with safe system fallback for tests and compatibility)
val Montserrat: FontFamily by lazy {
    if (isTesting) {
        FontFamily.SansSerif
    } else {
        try {
            FontFamily(
                Font(R.font.montserrat_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
                Font(R.font.montserrat_bold, FontWeight.Bold, loadingStrategy = FontLoadingStrategy.OptionalLocal)
            )
        } catch (e: Exception) {
            FontFamily.SansSerif
        }
    }
}

val NotoSansDevanagari: FontFamily by lazy {
    if (isTesting) {
        FontFamily.SansSerif
    } else {
        try {
            FontFamily(
                Font(R.font.notosansdevanagari_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
                Font(R.font.notosansdevanagari_bold, FontWeight.Bold, loadingStrategy = FontLoadingStrategy.OptionalLocal)
            )
        } catch (e: Exception) {
            FontFamily.SansSerif
        }
    }
}

private val LightColorScheme = lightColorScheme(
    primary = SaffronOrange,
    secondary = GoldenOrange,
    tertiary = DeepOrange,
    background = OffWhite,
    surface = OffWhite,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = SaffronOrange,
    secondary = GoldenOrange,
    tertiary = DeepOrange,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun VVFSmartFileManagerUltraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            bodyLarge = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            titleLarge = TextStyle(
                fontFamily = NotoSansDevanagari,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
            bodySmall = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            ),
            titleMedium = TextStyle(
                fontFamily = NotoSansDevanagari,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            )
        ),
        content = content
    )
}

