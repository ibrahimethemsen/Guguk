package com.ibrahimethemsen.guguk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * JSON şablonları için data class
 */
data class JsonTemplate(
    val name: String,
    val description: String,
    val template: String
)

/**
 * Önceden tanımlanmış JSON şablonları
 */
object JsonTemplates {
    val templates = listOf(
        JsonTemplate(
            name = "Basit Obje",
            description = "Temel JSON objesi",
            template = """{
  "name": "ibrahim",
  "value": 50,
  "active": true
}"""
        ),
        JsonTemplate(
            name = "Kullanıcı",
            description = "Kullanıcı bilgileri",
            template = """{
  "id": 1,
  "name": "İbrahim Ethem",
  "email": "guguk@example.com",
  "age": 1453,
  "isActive": true,
  "roles": ["developer", "android"]
}"""
        ),
        JsonTemplate(
            name = "Ürün",
            description = "E-ticaret ürün bilgileri",
            template = """{
  "id": "PROD-001",
  "name": "Akıllı Telefon",
  "price": 1299.99,
  "currency": "TRY",
  "category": "Elektronik",
  "inStock": true,
  "specifications": {
    "brand": "Samsung",
    "model": "Galaxy S21",
    "storage": "128GB"
  }
}"""
        ),
        JsonTemplate(
            name = "API Yanıtı",
            description = "Standart API yanıt formatı",
            template = """{
  "success": true,
  "message": "İşlem başarılı",
  "data": {
    "items": [],
    "total": 0,
    "page": 1,
    "limit": 10
  },
  "timestamp": "2024-01-01T00:00:00Z"
}"""
        ),
        JsonTemplate(
            name = "Hata Yanıtı",
            description = "Hata durumu için yanıt",
            template = """{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Geçersiz veri",
    "details": {
      "field": "email",
      "reason": "Geçersiz email formatı"
    }
  },
  "timestamp": "2024-01-01T00:00:00Z"
}"""
        )
    )
}

/**
 * JSON şablon seçici composable
 */
@Composable
fun JsonTemplateSelector(
    onTemplateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<JsonTemplate?>(null) }

    Column(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (selectedTemplate != null) "Template:${selectedTemplate!!.name}" else "Template")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color(0xFF1E1E1E))
                .border(1.dp, Color(0xFF3E4451), RoundedCornerShape(8.dp))
        ) {
            JsonTemplates.templates.forEach { template ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = template.name,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE4E6EA)
                            )
                            Text(
                                text = template.description,
                                fontSize = 12.sp,
                                color = Color(0xFF858585)
                            )
                        }
                    },
                    onClick = {
                        selectedTemplate = template
                        onTemplateSelected(template.template)
                        expanded = false
                    },
                    modifier = Modifier
                        .background(Color(0xFF1E1E1E))
                        .clickable { }
                )
            }
        }
    }
}