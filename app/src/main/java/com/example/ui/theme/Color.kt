package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Learning palette ────────────────────────────────────────────────
// Indigo keeps attention calm and focused, coral adds warmth and motivation,
// mint signals growth and success. Backgrounds are warm and low-glare for long sessions.
val BrandIndigo = Color(0xFF5B5BD6)
val BrandIndigoDark = Color(0xFF4343B8)
val BrandIndigoLight = Color(0xFF9B9CF2)
val BrandIndigoSoft = Color(0xFFECECFD)

val WarmCoral = Color(0xFFFF7A59)
val WarmCoralLight = Color(0xFFFFB199)
val WarmCoralSoft = Color(0xFFFFEDE7)

val FreshMint = Color(0xFF14B88F)
val FreshMintLight = Color(0xFF5EE0BC)
val FreshMintSoft = Color(0xFFDDF7EF)

val SunnyAmber = Color(0xFFF5A524)
val SunnyAmberSoft = Color(0xFFFFF3D6)
val SoftRose = Color(0xFFE5484D)
val SkyBlue = Color(0xFF3E9BF0)
val Lavender = Color(0xFF9D7BF0)

// Collection identity colors
val ToeflColor = BrandIndigo
val AwlColor = FreshMint

// Surfaces
val CreamBackground = Color(0xFFFAF8F5)
val CardWhite = Color(0xFFFFFFFF)
val LavenderMist = Color(0xFFF2F1FA)
val InkText = Color(0xFF1F1D36)
val MutedText = Color(0xFF6B6984)
val SoftOutline = Color(0xFFDCDAEA)

val NightBackground = Color(0xFF14131F)
val NightCard = Color(0xFF1F1D2E)
val NightCardRaised = Color(0xFF2A2840)
val NightText = Color(0xFFF2F1FA)
val NightMuted = Color(0xFFA9A7C2)
val NightOutline = Color(0xFF3D3A56)

// Gradients used by hero cards and buttons
val HeroGradient = Brush.linearGradient(listOf(BrandIndigo, Lavender))
val MotivationGradient = Brush.linearGradient(listOf(WarmCoral, SunnyAmber))
val GrowthGradient = Brush.linearGradient(listOf(FreshMint, SkyBlue))

// ── Legacy names kept so existing screens pick up the new palette ──
val PrimaryBlue = BrandIndigo
val PrimaryBlueDark = BrandIndigoDark
val PrimaryBlueLight = BrandIndigoLight
val AccentGold = SunnyAmber
val AccentGoldLight = Color(0xFFFFC95C)
val SuccessGreen = FreshMint
val SuccessGreenLight = FreshMintLight
val WarningOrange = WarmCoral
val ErrorRed = SoftRose
val SecondaryIndigo = Lavender

val Navy900 = InkText
val Navy800 = NightCard
val Navy700 = NightCardRaised
val Navy600 = NightOutline

val SlateLightBg = CreamBackground
val SlateLightCard = CardWhite
val SlateDarkBg = NightBackground
val SlateDarkCard = NightCard

val TextPrimaryLight = InkText
val TextSecondaryLight = MutedText
val TextPrimaryDark = NightText
val TextSecondaryDark = NightMuted
