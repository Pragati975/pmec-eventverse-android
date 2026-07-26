package com.pmec.eventverse.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pmec.eventverse.ui.theme.*
import kotlin.random.Random
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

// ---------- Background ----------

private val SpaceBgTop = Color(0xFF0A0E27)
private val SpaceBgBottom = Color(0xFF05060F)

@Composable
fun SpaceBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(SpaceBgTop, SpaceBgBottom)))
    ) {
        val width = maxWidth
        val height = maxHeight
        val dots = remember {
            List(18) {
                Triple(
                    Random.nextInt(0, 1000) / 1000f,
                    Random.nextInt(0, 1000) / 1000f,
                    Random.nextInt(2, 4)
                )
            }
        }
        dots.forEach { (xFraction, yFraction, sizeDp) ->
            Box(
                modifier = Modifier
                    .offset(x = width * xFraction, y = height * yFraction)
                    .size(sizeDp.dp)
                    .background(AccentPurple.copy(alpha = 0.55f), CircleShape)
            )
        }
        content()
    }
}

// ---------- Slanted button shape (matches the angled buttons in the reference) ----------

class SlantedButtonShape(private val cut: Dp = 14.dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): Outline {
        val cutPx = with(density) { cut.toPx() }
        val path = Path().apply {
            moveTo(cutPx, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - cutPx, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

// ---------- Gradient primary button ----------

@Composable
fun GradientAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val isActive = enabled && !loading
    val gradientColors = if (isActive) {
        listOf(AccentPurple, AccentBlue)
    } else {
        listOf(AccentPurple.copy(alpha = 0.4f), AccentBlue.copy(alpha = 0.4f))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(SlantedButtonShape())
            .background(Brush.horizontalGradient(colors = gradientColors))
            .then(
                Modifier.clickable(enabled = isActive) { onClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ---------- Google sign-in button ----------

@Composable
fun GoogleSignInButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceDark),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
    ) {
        Image(
            painter = painterResource(id = com.pmec.eventverse.R.drawable.ic_google_logo),
            contentDescription = "Google",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

// ---------- "or ..." divider ----------

@Composable
fun OrDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = SurfaceDark)
        Text(
            text,
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Divider(modifier = Modifier.weight(1f), color = SurfaceDark)
    }
}

// ---------- Styled labeled field (uppercase label above a minimal bordered box) ----------

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            label.uppercase(),
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                androidx.compose.material.icons.Icons.Filled.Visibility
                            else androidx.compose.material.icons.Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = TextMuted
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = CardDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}