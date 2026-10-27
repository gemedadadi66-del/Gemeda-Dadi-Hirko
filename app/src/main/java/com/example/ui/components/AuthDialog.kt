package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.AppLanguage
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SealCrimson
import com.example.ui.theme.SkyAccent

@Composable
fun AuthDialog(
    currentUser: User?,
    language: AppLanguage,
    userDocumentCount: Int,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onLogin: (identifier: String, password: String) -> Unit,
    onRegister: (username: String, email: String, password: String, fullName: String, organization: String, role: String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Login, 1 = Register

    // Login fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register fields
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regFullName by remember { mutableStateOf("") }
    var regOrganization by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SkyAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentUser != null) Icons.Default.VerifiedUser else Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = if (currentUser != null) EmeraldSuccess else SkyAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (currentUser != null) {
                            if (language == AppLanguage.OROMO) "Akkaawuntii Fayyadamaa" else "User Account"
                        } else {
                            if (language == AppLanguage.OROMO) "Seensa & Galmee Fayyadamaa" else "Login & Registration"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentUser != null) {
                            if (language == AppLanguage.OROMO) "Galmaa'eera (Active Session)" else "Authenticated Session"
                        } else {
                            if (language == AppLanguage.OROMO) "Waraqaalee keessan eeggachuuf seenaa" else "Sign in to secure your documents"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // If user is currently logged in, show user profile card with logout
                if (currentUser != null) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(EmeraldSuccess, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.fullName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = currentUser.fullName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "@${currentUser.username} • ${currentUser.email}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Dhaabbata" else "Organization",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currentUser.organization.ifEmpty { "Oromia Gov" },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Gahee Hojii" else "Role",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = currentUser.roleTitle.ifEmpty { "Gulaalaa Waraqaa" },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Waraqaalee" else "Documents",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$userDocumentCount Saved",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SealCrimson,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_logout")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.OROMO) "Ba'i (Sign Out)" else "Sign Out",
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else {
                    // Tabs: Login vs Register
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .padding(bottom = 12.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    if (language == AppLanguage.OROMO) "Seensa (Login)" else "Login",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    if (language == AppLanguage.OROMO) "Galmee (Register)" else "Register",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = SealCrimson,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (selectedTab == 0) {
                        // LOGIN FORM
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { loginIdentifier = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Username ykn Email" else "Username or Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("input_login_username")
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Jecha Iccitii (Password)" else "Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                    Icon(
                                        imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("input_login_password")
                        )

                        Button(
                            onClick = {
                                onLogin(loginIdentifier.trim(), loginPassword)
                            },
                            enabled = loginIdentifier.isNotBlank() && loginPassword.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkyAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_submit_login")
                        ) {
                            Text(
                                text = if (language == AppLanguage.OROMO) "Seeni (Sign In)" else "Sign In",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Demo account quick filler
                        OutlinedButton(
                            onClick = {
                                loginIdentifier = "admin"
                                loginPassword = "password123"
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.OROMO) "Akkaawuntii Yaalii (Quick Demo Fill)" else "Quick Demo Account Fill",
                                fontSize = 12.sp
                            )
                        }

                    } else {
                        // REGISTRATION FORM
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Username" else "Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("input_reg_username")
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("input_reg_email")
                        )

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Maqaa Guutuu" else "Full Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("input_reg_name")
                        )

                        OutlinedTextField(
                            value = regOrganization,
                            onValueChange = { regOrganization = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Dhaabbata / Biiroo" else "Organization") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("input_reg_org")
                        )

                        OutlinedTextField(
                            value = regRole,
                            onValueChange = { regRole = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Gahee Hojii / Itti-gaafatama" else "Role / Job Title") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("input_reg_role")
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text(if (language == AppLanguage.OROMO) "Jecha Iccitii Haaraa" else "Password (min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                    Icon(
                                        imageVector = if (showRegPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("input_reg_password")
                        )

                        Button(
                            onClick = {
                                onRegister(
                                    regUsername.trim(),
                                    regEmail.trim(),
                                    regPassword,
                                    regFullName.trim(),
                                    regOrganization.trim(),
                                    regRole.trim()
                                )
                            },
                            enabled = regUsername.isNotBlank() && regEmail.isNotBlank() && regPassword.length >= 4,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_submit_register")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.OROMO) "Galmaa'i (Register)" else "Register Account",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (language == AppLanguage.OROMO) "Cufi" else "Close")
            }
        }
    )
}
