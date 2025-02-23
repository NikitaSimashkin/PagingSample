package ru.kram.pagingsample.ui.catlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.kram.pagingsample.R
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme
import ru.kram.pagingsample.ui.catlist.model.CatItemData

@Composable
fun CatItem(
    catItemData: CatItemData,
    onDeleteClick: (catId: String) -> Unit,
    onRenameClick: (catId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(100.dp)
            .fillMaxWidth(),
    ) {
        AsyncImage(
            model = catItemData.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(end = 16.dp)
                .aspectRatio(1f)
                .clip(CircleShape),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Name: ${catItemData.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Colors.textPrimary,
            )
            Text(
                text = "Breed: ${catItemData.breed}",
                style = MaterialTheme.typography.bodyMedium,
                color = Colors.textPrimary,
            )
            Text(
                text = "Age: ${catItemData.age}",
                style = MaterialTheme.typography.bodyMedium,
                color = Colors.textPrimary,
            )
        }

        Box(
            modifier = Modifier
        ) {
            val isPopupExpanded = remember { mutableStateOf(false) }

            IconButton(
                onClick = { isPopupExpanded.value = true },
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = Colors.primary
                )
            }

            ActionsPopup(
                catId = catItemData.id,
                expanded = isPopupExpanded,
                onDeleteClick = onDeleteClick,
                onRenameClick = onRenameClick
            )
        }
    }
}

@Composable
fun ActionsPopup(
    catId: String,
    expanded: MutableState<Boolean>,
    onDeleteClick: (catId: String) -> Unit,
    onRenameClick: (catId: String) -> Unit,

    modifier: Modifier = Modifier
) {
    DropdownMenu(
        modifier = modifier,
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false }
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.rename),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Colors.textPrimary
                )
            },
            onClick = {
                expanded.value = false
                onRenameClick(catId)
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.delete),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Colors.textPrimary
                )
            },
            onClick = {
                expanded.value = false
                onDeleteClick(catId)
            }
        )
    }
}

@Preview
@Composable
private fun CatItemPreview() {
    PagingSampleTheme {
        Box(modifier = Modifier.background(Color.White)) {
            CatItem(
                catItemData = CatItemData(
                    id = "1",
                    imageUrl = "123",
                    name = "Kitty",
                    breed = "British Shorthair",
                    age = 2,
                    createdAt = System.currentTimeMillis(),
                ),
                onDeleteClick = { },
                onRenameClick = { }
            )
        }
    }
}