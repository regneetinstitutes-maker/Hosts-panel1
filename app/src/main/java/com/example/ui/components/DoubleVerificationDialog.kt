package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Foolproof Double Verification Dialog.
 * Step 1: Input values -> Confirm.
 * Step 2: Re-enter values (first values hidden) -> Confirm.
 * Verifies that First == Second on submit.
 */
@Composable
fun DoubleVerificationDialog(
    title: String,
    primaryFieldLabel: String,
    secondaryFieldLabel: String? = null,
    isNumeric: Boolean = false,
    isSecret: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmed: (firstVal1: String, firstVal2: String, secondVal1: String, secondVal2: String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var firstField1 by remember { mutableStateOf("") }
    var firstField2 by remember { mutableStateOf("") }
    var secondField1 by remember { mutableStateOf("") }
    var secondField2 by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("double_verification_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Double Verification",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (step == 1) "Step 1 of 2: Enter Details" else "Step 2 of 2: Re-enter for Verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (localError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                if (step == 1) {
                    Text(
                        text = "Please enter the information carefully and accurately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = firstField1,
                        onValueChange = {
                            firstField1 = it
                            localError = null
                        },
                        label = { Text(primaryFieldLabel) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("step1_field1_input")
                    )

                    if (secondaryFieldLabel != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = firstField2,
                            onValueChange = {
                                firstField2 = it
                                localError = null
                            },
                            label = { Text(secondaryFieldLabel) },
                            singleLine = true,
                            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("step1_field2_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("step1_cancel_button")
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (firstField1.isBlank() || (secondaryFieldLabel != null && firstField2.isBlank())) {
                                    localError = "Please fill in all required fields."
                                } else {
                                    localError = null
                                    step = 2
                                }
                            },
                            modifier = Modifier.testTag("step1_confirm_button")
                        ) {
                            Text("Confirm →")
                        }
                    }
                } else {
                    // STEP 2: Re-enter values (First values hidden)
                    Text(
                        text = "Re-enter the details below to verify matching values.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = secondField1,
                        onValueChange = {
                            secondField1 = it
                            localError = null
                        },
                        label = { Text("Re-enter $primaryFieldLabel") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("step2_field1_input")
                    )

                    if (secondaryFieldLabel != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = secondField2,
                            onValueChange = {
                                secondField2 = it
                                localError = null
                            },
                            label = { Text("Re-enter $secondaryFieldLabel") },
                            singleLine = true,
                            visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("step2_field2_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                // Reset to step 1
                                step = 1
                                secondField1 = ""
                                secondField2 = ""
                                localError = null
                            },
                            modifier = Modifier.testTag("step2_back_button")
                        ) {
                            Text("← Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (firstField1.trim() != secondField1.trim() ||
                                    (secondaryFieldLabel != null && firstField2.trim() != secondField2.trim())
                                ) {
                                    localError = "You haven't entered information correctly. Please re-enter carefully and with concentration."
                                } else {
                                    onConfirmed(firstField1, firstField2, secondField1, secondField2)
                                }
                            },
                            modifier = Modifier.testTag("step2_confirm_button")
                        ) {
                            Text("Confirm & Submit")
                        }
                    }
                }
            }
        }
    }
}
