package tvnclr.flightdeck.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ── Dialogs ──────────────────────────────────────────────────────────────────

/** Confirmation dialog. [destructive] colors the confirm action with the danger tone. */
@Composable
fun FdConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
    icon: ImageVector? = if (destructive) Icons.Rounded.WarningAmber else null,
) {
  val c = Fd.colors
  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = c.card,
      shape = FdShape.card,
      icon = icon?.let { { Icon(it, null, tint = if (destructive) c.danger else c.accent) } },
      title = { Text(title, style = FdType.title, color = c.textPrimary) },
      text = { Text(text, style = FdType.body, color = c.textSecondary) },
      confirmButton = {
        FdTextButton(confirmText, onConfirm, color = if (destructive) c.danger else c.accent)
      },
      dismissButton = dismissText?.let { { FdTextButton(it, onDismiss, color = c.textSecondary) } },
  )
}

@Composable
fun FdTextButton(text: String, onClick: () -> Unit, color: Color = Fd.colors.accent, enabled: Boolean = true) {
  Surface(onClick = onClick, enabled = enabled, shape = FdShape.pill, color = Color.Transparent) {
    Text(
        text,
        style = FdType.item,
        color = color.copy(alpha = if (enabled) 1f else 0.4f),
        modifier = Modifier.heightIn(min = 40.dp).padding(horizontal = FdSpace.l, vertical = 10.dp),
    )
  }
}

/** Single text input dialog with validation. */
@Composable
fun FdInputDialog(
    title: String,
    placeholder: String,
    confirmText: String,
    dismissText: String,
    isValid: (String) -> Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    monospace: Boolean = true,
) {
  val c = Fd.colors
  var value by remember { mutableStateOf("") }
  AlertDialog(
      onDismissRequest = onDismiss,
      containerColor = c.card,
      shape = FdShape.card,
      title = { Text(title, style = FdType.title, color = c.textPrimary) },
      text = {
        FdTextField(value, { value = it }, placeholder = placeholder, monospace = monospace, singleLine = false, minHeight = 96)
      },
      confirmButton = { FdTextButton(confirmText, { onConfirm(value) }, enabled = value.isNotBlank() && isValid(value)) },
      dismissButton = { FdTextButton(dismissText, onDismiss, color = c.textSecondary) },
  )
}

@Composable
fun FdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    monospace: Boolean = false,
    singleLine: Boolean = true,
    minHeight: Int = 0,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
  val c = Fd.colors
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = modifier.fillMaxWidth().heightIn(min = minHeight.dp),
      enabled = enabled,
      label = label?.let { { Text(it) } },
      placeholder = placeholder?.let { { Text(it, color = c.textSecondary) } },
      leadingIcon = leadingIcon?.let { { Icon(it, null, tint = c.textSecondary) } },
      singleLine = singleLine,
      textStyle = if (monospace) FdType.mono.copy(color = c.textPrimary, fontSize = FdType.body.fontSize) else FdType.body.copy(color = c.textPrimary),
      shape = FdShape.inner,
      colors =
          OutlinedTextFieldDefaults.colors(
              focusedBorderColor = c.accent,
              unfocusedBorderColor = c.cardBorder,
              focusedLabelColor = c.accent,
              cursorColor = c.accent,
              focusedContainerColor = c.elevated.copy(alpha = 0.5f),
              unfocusedContainerColor = c.elevated.copy(alpha = 0.5f),
          ),
  )
}

@Composable
fun FdSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
  FdTextField(value, onValueChange, placeholder = placeholder, leadingIcon = Icons.Rounded.Search)
}

// ── Menus ────────────────────────────────────────────────────────────────────

data class FdMenuItem(val text: String, val icon: ImageVector? = null, val destructive: Boolean = false, val onClick: () -> Unit)

/** Overflow (⋮) button with a card-styled menu. */
@Composable
fun FdOverflowMenu(items: List<FdMenuItem>, contentDescription: String = androidx.compose.ui.res.stringResource(R.string.fd_options)) {
  val c = Fd.colors
  var open by remember { mutableStateOf(false) }
  Box {
    FdIconButton(Icons.Rounded.MoreVert, contentDescription, { open = true })
    DropdownMenu(
        expanded = open,
        onDismissRequest = { open = false },
        containerColor = c.card,
        shape = FdShape.inner,
        border = BorderStroke(1.dp, c.cardBorder),
    ) {
      items.forEach { item ->
        val color = if (item.destructive) c.danger else c.textPrimary
        DropdownMenuItem(
            text = { Text(item.text, style = FdType.body, color = color) },
            leadingIcon = item.icon?.let { { Icon(it, null, tint = if (item.destructive) c.danger else c.accent) } },
            onClick = {
              open = false
              item.onClick()
            },
        )
      }
    }
  }
}

// ── Choice & empty states ────────────────────────────────────────────────────

/** Selectable card (radio semantics) with icon, title and description. */
@Composable
fun FdChoiceCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
  val c = Fd.colors
  val alpha = if (enabled) 1f else 0.45f
  Surface(
      onClick = onClick,
      enabled = enabled,
      modifier = Modifier.fillMaxWidth(),
      shape = FdShape.card,
      color = if (selected) c.accentSoft else c.card,
      border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.accent else c.cardBorder),
  ) {
    Row(
        Modifier.padding(FdSpace.l),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(FdSpace.m),
    ) {
      FdIconBadge(icon, tint = c.accent.copy(alpha = alpha), background = if (selected) c.accent.copy(alpha = 0.2f) else c.accentSoft)
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FdSpace.xs)) {
        Text(title, style = FdType.title, color = c.textPrimary.copy(alpha = alpha))
        Text(description, style = FdType.caption, color = c.textSecondary.copy(alpha = alpha))
      }
      Box(
          Modifier.size(22.dp)
              .clip(CircleShape)
              .border(2.dp, if (selected) c.accent else c.textSecondary.copy(alpha = alpha), CircleShape),
          contentAlignment = Alignment.Center,
      ) {
        if (selected) Box(Modifier.size(11.dp).clip(CircleShape).background(c.accent))
      }
    }
  }
}

/** Empty / error state inside a card. */
@Composable
fun FdEmptyState(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
  val c = Fd.colors
  Column(
      modifier.fillMaxWidth().padding(vertical = FdSpace.xxl, horizontal = FdSpace.l),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(FdSpace.m),
  ) {
    FdIconBadge(icon, tint = c.textSecondary, background = c.elevated, size = 56.dp)
    Text(text, style = FdType.body, color = c.textSecondary, textAlign = TextAlign.Center)
  }
}

/** Inline callout for warnings / tips inside or outside cards. */
@Composable
fun FdCallout(text: String, status: FdStatus = FdStatus.WARN, icon: ImageVector = Icons.Rounded.WarningAmber) {
  val color = fdStatusColor(status)
  Row(
      Modifier.fillMaxWidth().clip(FdShape.inner).background(color.copy(alpha = 0.10f)).padding(FdSpace.m),
      horizontalArrangement = Arrangement.spacedBy(FdSpace.m),
      verticalAlignment = Alignment.Top,
  ) {
    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
    Text(text, style = FdType.caption, color = Fd.colors.textPrimary, modifier = Modifier.weight(1f))
  }
}

/** Single-choice chips that wrap onto several lines (for option sets too big for FdSegmented). */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FdChoiceChips(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
  val c = Fd.colors
  androidx.compose.foundation.layout.FlowRow(
      modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(FdSpace.s),
      verticalArrangement = Arrangement.spacedBy(FdSpace.s),
  ) {
    options.forEachIndexed { i, label ->
      val sel = i == selected
      Surface(
          onClick = { onSelect(i) },
          shape = FdShape.pill,
          color = if (sel) c.accent else c.elevated,
          border = BorderStroke(1.dp, if (sel) c.accent else c.cardBorder),
      ) {
        Text(
            label,
            style = FdType.label,
            color = if (sel) c.onAccent else c.textSecondary,
            modifier = Modifier.heightIn(min = 36.dp).padding(horizontal = FdSpace.l, vertical = 9.dp),
        )
      }
    }
  }
}
