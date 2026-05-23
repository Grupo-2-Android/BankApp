package com.example.bankapp.presentation.utils.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.R
import com.example.bankapp.data.local.room.entities.Card
import com.example.bankapp.presentation.theme.PhysicalCardColor
import com.example.bankapp.presentation.theme.VirtualCardColor
import com.example.bankapp.presentation.utils.formatCardNumber
import com.example.bankapp.presentation.utils.hideCardNumber
import com.example.bankapp.presentation.viewmodels.cards.CardManagementViewModel

@Composable
fun CardItem(card: Card, isAdding: Boolean = false) {
    val brandLogoRes = when (card.brand.trim().uppercase()) {
        "VISA" -> R.drawable.visa
        "MASTERCARD" -> R.drawable.mastercard
        "ELO" -> R.drawable.elo
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.type == "VIRTUAL") VirtualCardColor else PhysicalCardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (brandLogoRes != null) {
                    Image(
                        painter = painterResource(id = brandLogoRes),
                        contentDescription = card.brand,
                        modifier = Modifier.height(24.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = card.brand,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = if (card.type == CardManagementViewModel.TYPE_PHYSICAL) "Físico" else "Virtual",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Text(
                text = if (isAdding) formatCardNumber(card.number) else hideCardNumber(card.number),
                color = Color.White,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "VALID THRU",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = card.expiration,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CVV",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (isAdding) card.cvv else "***",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}