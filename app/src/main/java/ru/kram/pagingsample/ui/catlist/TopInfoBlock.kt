import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme
import ru.kram.pagingsample.ui.catlist.model.InfoBlockData

@Composable
fun TopInfoBlock(
    infoBlockData: InfoBlockData,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(0.8f)
                .padding(end = 8.dp)
        ) {
            TextBlock(
                textLeft = infoBlockData.text1Left,
                textRight = infoBlockData.text1Right
            )
            TextBlock(
                textLeft = infoBlockData.text2Left,
                textRight = infoBlockData.text2Right
            )
            TextBlock(
                textLeft = infoBlockData.text3Left,
                textRight = infoBlockData.text3Right
            )
        }

        Column(
            modifier = Modifier
                .weight(0.1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = onAddClick,
                vector = Icons.Default.Add,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onSettingsClick,
                vector = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TextBlock(
    textLeft: String?,
    textRight: String?,
    modifier: Modifier = Modifier
) {
    val color = Colors.textPrimary
    val typography = MaterialTheme.typography.labelLarge

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (textLeft != null) {
            Text(
                text = textLeft,
                color = color,
                style = typography,
                modifier = Modifier.weight(0.5f)
            )
        }
        if (textRight != null) {
            Text(
                text = textRight,
                color = color,
                style = typography,
                modifier = Modifier.weight(0.5f)
            )
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    vector: ImageVector,
    modifier: Modifier = Modifier,
) {
    PrimaryActionButton(
        vector = vector,
        modifier = modifier,
        onClick = onClick
    )
}

@Preview
@Composable
private fun TopInfoBlockPreview(modifier: Modifier = Modifier) {
    PagingSampleTheme {
        Box(modifier = modifier.background(Colors.background)) {
            TopInfoBlock(
                onAddClick = {},
                onSettingsClick = {},
                infoBlockData = InfoBlockData(
                    text1Left = "Text 1",
                    text1Right = "Text 1",
                    text2Left = "Text 2",
                    text2Right = "Text 2",
                    text3Left = "Text 3",
                    text3Right = "Text 3"
                ),
            )
        }
    }
}