import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kram.pagingsample.designsystem.button.PulseButton
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme

@Composable
fun PrimaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PrimaryActionButton(
        modifier = modifier
            .height(48.dp),
        onClick = onClick,
        afterModifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun PrimaryActionButton(
    vector: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PrimaryActionButton(
        modifier = modifier
            .size(40.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun PrimaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    afterModifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PulseButton(
        onClick = onClick,
        modifier = modifier
            .background(
                color = Colors.primary,
                shape = RoundedCornerShape(12.dp),
            )
            .then(afterModifier),
        content
    )
}

@Preview
@Composable
private fun PrimaryActionButtonTextPreview() {
    PagingSampleTheme {
        PrimaryActionButton(text = "Click me", onClick = {})
    }
}

@Preview
@Composable
private fun PrimaryActionButtonIconPreview() {
    PagingSampleTheme {
        PrimaryActionButton(
            vector = Icons.Default.Add,
            onClick = {}
        )
    }
}