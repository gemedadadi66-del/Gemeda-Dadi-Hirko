package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.EditedDocument
import com.example.domain.processor.DocumentImageProcessor
import com.example.ui.AppLanguage
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SkyAccent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentHistorySheet(
    documents: List<EditedDocument>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSelectDocument: (EditedDocument) -> Unit,
    onDeleteDocument: (EditedDocument) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SkyAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.OROMO) "Galmee Qophaa'an (Saved Documents)" else "Saved Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${documents.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.OROMO) "Ammaaf waraqaan hin saavamne" else "No saved documents yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (language == AppLanguage.OROMO) "Waraqaa gulaalaatii PDF buufadhaa!" else "Edit documents and export PDF to see them here!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail
                                val imgFile = File(doc.editedImagePath)
                                if (imgFile.exists()) {
                                    AsyncImage(
                                        model = imgFile,
                                        contentDescription = "Thumbnail",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp, 68.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                onSelectDocument(doc)
                                                onDismiss()
                                            }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 68.dp)
                                            .background(Color.LightGray, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Gray)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onSelectDocument(doc)
                                            onDismiss()
                                        }
                                ) {
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(doc.dateModified)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Chaappaa: ${doc.stampWidth}×${doc.stampHeight}px",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Quick actions: Open PDF, Share, Delete
                                if (!doc.pdfPath.isNullOrEmpty() && File(doc.pdfPath).exists()) {
                                    val pdfFile = File(doc.pdfPath)
                                    IconButton(
                                        onClick = {
                                            openPdfIntent(context, pdfFile)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Open PDF",
                                            tint = SkyAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            sharePdfIntent(context, pdfFile)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share PDF",
                                            tint = EmeraldSuccess,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteDocument(doc) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openPdfIntent(context: Context, file: File) {
    try {
        val uri: Uri = DocumentImageProcessor.getContentUri(context, file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "No PDF viewer found: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun sharePdfIntent(context: Context, file: File) {
    try {
        val uri: Uri = DocumentImageProcessor.getContentUri(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share PDF Document"))
    } catch (e: Exception) {
        Toast.makeText(context, "Could not share: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
