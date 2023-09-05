package com.example.vflix

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BelowLoginScreen() {
    Column(Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "Forgot Password ?",
                fontSize = 12.sp,
                color = Color.White,
                fontFamily = FontFamily.Serif,
                //fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(id = R.drawable.max_quilt_0),
                    contentScale = ContentScale.Crop,
                )
                .padding(16.dp),
        ) {

            Text(
                text = ":)",
                fontSize = 30.sp,
                modifier = Modifier.padding(8.dp),
                color = Color.Magenta,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Start
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 30.sp,
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                )

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.padding(8.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    shape = MaterialTheme.shapes.small,
                    label = { Text("Email") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.padding(8.dp),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    trailingIcon = {
                        Box(modifier = Modifier.clickable {
                            passwordVisibility = !passwordVisibility
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Lock",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    },
                    shape = MaterialTheme.shapes.small
                )

                val isButtonEnabled = email.isNotEmpty() && password.isNotEmpty()
                val buttonColor = remember { mutableStateOf(Color.Red) }

                Button(
                    onClick = {
                        buttonColor.value =
                            if (buttonColor.value == Color.Black) Color.Red else Color.Black
                    },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        disabledContainerColor = Color.Red,
                    ),
                    shape = MaterialTheme.shapes.extraSmall,
                    border = BorderStroke(2.dp, buttonColor.value),
                    enabled = isButtonEnabled,
                    contentPadding = PaddingValues(
                        horizontal = 113.dp,
                        vertical = 11.dp
                    )
                ) {
                    Text("Sign In",
                        fontSize = 16.sp,
                        color = Color.White)
                }

                BelowLoginScreen()
                NewToVFX()
                LoginFooter()
            }
        }
    }
}

@Composable
fun NewToVFX() {
    Column {
        Row {
            Text(
                text = "New to VFlix? ",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Serif,
            )

            Text(
                text = "Sign Up",
                fontSize = 12.sp,
                color = Color.Blue,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        enabled = true,
                        onClick = { /*TODO*/ },
                        onClickLabel = "Sign Up"
                    )
            )
        }
    }
}

@Composable
fun LoginFooter() {

    Divider(
        color = Color.White,
        modifier = Modifier
            .padding(
                vertical = 16.dp
            )
            .height(1.dp)
    )

    Column {
        Row {
            Text(
                text = "By signing in, you agree to ",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Serif,
            )

            Text(text = "VFlix's",
                fontSize = 12.sp,
                color = Color.Red,
                fontFamily = FontFamily.Serif,
            )

            Text(
                text = " Terms and Conditions",
                fontSize = 12.sp,
                color = Color(0xFF4A8DB9),
                fontFamily = FontFamily.Serif,
                modifier = Modifier.clickable(
                    enabled = true,
                    onClick = { /*TODO*/ },
                    onClickLabel = "Terms and Conditions"
                )
            )
        }

        Text(
            text = "App Version: 0.0.1alpha",
            fontSize = 12.sp,
            color = Color(0xFFFFA500),
            fontFamily = FontFamily.Serif,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            LanguageSelector()
        }
    }
}

@Composable
fun LanguageSelector() {
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    val languages = listOf("English", "Spanish", "French", "German")

    Box(modifier =  Modifier
        .wrapContentSize()
        .clip(RoundedCornerShape(1.dp))
        .border(
            width = 1.dp,
            color = Color.White,
            shape = RoundedCornerShape(1.dp)
        ).background(Color.Transparent
        ))
        {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Globe Icon",
                tint = Color.Green,
                modifier = Modifier.padding(
                    horizontal = 2.dp,
                    vertical = 4.dp
                )
            )
        Text(
            text = selectedLanguage,
            fontSize = 12.sp,
            color = Color.White,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.clickable { expanded = true }.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ).padding(
                start = 8.dp
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(text = language) },
                    onClick = {
                        selectedLanguage = language
                        expanded = false
                    }
                )
            }
        }
    }
}