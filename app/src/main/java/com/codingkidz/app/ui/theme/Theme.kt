package com.codingkidz.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codingkidz.app.data.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFFF97316),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF431407),
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF042F2E),
    background = Color(0xFFF7F7FF),
    onBackground = Color(0xFF1B1B2F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B2F),
    surfaceVariant = Color(0xFFEEF0FA),
    onSurfaceVariant = Color(0xFF4A4C63),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F8FF),
    surfaceContainer = Color(0xFFF1F2FC),
    surfaceContainerHigh = Color(0xFFEBECF8),
    surfaceContainerHighest = Color(0xFFE4E6F4),
    outline = Color(0xFF9497B0),
    outlineVariant = Color(0xFFD5D7E8),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF450A0A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFFDBA74),
    onSecondary = Color(0xFF431407),
    secondaryContainer = Color(0xFF9A3412),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = Color(0xFF5EEAD4),
    onTertiary = Color(0xFF042F2E),
    tertiaryContainer = Color(0xFF115E59),
    onTertiaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF11111F),
    onBackground = Color(0xFFE6E6F5),
    surface = Color(0xFF171728),
    onSurface = Color(0xFFE6E6F5),
    surfaceVariant = Color(0xFF2A2B40),
    onSurfaceVariant = Color(0xFFC3C5DB),
    surfaceContainerLowest = Color(0xFF0D0D19),
    surfaceContainerLow = Color(0xFF181829),
    surfaceContainer = Color(0xFF1D1D30),
    surfaceContainerHigh = Color(0xFF26273B),
    surfaceContainerHighest = Color(0xFF313247),
    outline = Color(0xFF8D8FA8),
    outlineVariant = Color(0xFF3F4058),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
)

private val KidTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.Black),
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.Black),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.Bold),
        titleSmall = t.titleSmall.copy(fontWeight = FontWeight.Bold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
        bodyLarge = t.bodyLarge.copy(lineHeight = 24.sp),
    )
}

private val KidShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Warna papan permainan, berbeda untuk mode terang dan gelap. */
@Immutable
data class GameColors(
    val boardBackground: Color,
    val floorA: Color,
    val floorB: Color,
    val wall: Color,
    val wallTop: Color,
    val blueTile: Color,
    val orangeTile: Color,
    val gem: Color,
    val gemShine: Color,
    val flag: Color,
    val pole: Color,
    val codeBackground: Color,
    val codeKeyword: Color,
    val codeFunction: Color,
    val codeNumber: Color,
    val codeString: Color,
    val codePlain: Color,
    val codeComment: Color,
)

private val LightGame = GameColors(
    boardBackground = Color(0xFF86C77A),
    floorA = Color(0xFFDDF4C9),
    floorB = Color(0xFFCDECB6),
    wall = Color(0xFF64748B),
    wallTop = Color(0xFF94A3B8),
    blueTile = Color(0xFF60A5FA),
    orangeTile = Color(0xFFFB923C),
    gem = Color(0xFF8B5CF6),
    gemShine = Color(0xFFE9D5FF),
    flag = Color(0xFFEF4444),
    pole = Color(0xFF6B4F3A),
    codeBackground = Color(0xFF1E1B4B),
    codeKeyword = Color(0xFFF472B6),
    codeFunction = Color(0xFF7DD3FC),
    codeNumber = Color(0xFFFDE047),
    codeString = Color(0xFF86EFAC),
    codePlain = Color(0xFFE0E7FF),
    codeComment = Color(0xFF818CF8),
)

private val DarkGame = LightGame.copy(
    boardBackground = Color(0xFF1F3B2A),
    floorA = Color(0xFF2F4F3A),
    floorB = Color(0xFF29473A),
    wall = Color(0xFF334155),
    wallTop = Color(0xFF475569),
    codeBackground = Color(0xFF0B0B1A),
)

val LocalGameColors = staticCompositionLocalOf { LightGame }
val LocalIsDark = staticCompositionLocalOf { false }

@Composable
fun CodingKidzTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colors: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    CompositionLocalProvider(
        LocalGameColors provides if (dark) DarkGame else LightGame,
        LocalIsDark provides dark,
    ) {
        MaterialTheme(colorScheme = colors, typography = KidTypography, shapes = KidShapes, content = content)
    }
}

/** Gaya teks monospace untuk tampilan kode. */
val CodeTextStyle = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 14.sp, lineHeight = 22.sp)
