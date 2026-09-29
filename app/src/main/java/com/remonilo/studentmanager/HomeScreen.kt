package com.remonilo.studentmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remonilo.studentmanager.ui.theme.StudentManagerTheme

private val ScreenBackground = Color(0xFFF6F8FC)
private val AvatarGrey = Color(0xFFD5D9E0)
private val HintGrey = Color(0xFF8A8F98)
private val DeleteRed = Color(0xFFE03030)

@Composable
fun HomeScreen(
    students: List<Student>,
    onAddClick: () -> Unit,
    onEditClick: (Student) -> Unit,
    onDeleteClick: (Student) -> Unit,
    onExit: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showAbout by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(students, query) {
        val q = query.trim()
        if (q.isEmpty()) students
        else students.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.nim.contains(q, ignoreCase = true) ||
                it.major.contains(q, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeTopBar(
                onRefresh = { query = "" },
                onAbout = { showAbout = true },
                onExit = onExit
            )
            SearchField(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Text(
                text = "Jumlah mahasiswa: ${filtered.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (filtered.isEmpty()) {
                EmptyState(isSearching = query.isNotBlank())
            } else {
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.nim }) { student ->
                        StudentCard(
                            student = student,
                            onEdit = { onEditClick(student) },
                            onDelete = { onDeleteClick(student) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddClick,
            containerColor = BluePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = "Tambah mahasiswa")
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Tentang Aplikasi") },
            text = {
                Text("Student Manager\nAplikasi sederhana untuk mengelola data mahasiswa.\nVersi 1.0")
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
private fun HomeTopBar(onRefresh: () -> Unit, onAbout: () -> Unit, onExit: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }

    Surface(color = HeaderGrey, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Student Manager",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(painterResource(R.drawable.ic_more_vert), contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    containerColor = Color.White
                ) {
                    MenuItem("Refresh", R.drawable.ic_refresh) { menuOpen = false; onRefresh() }
                    MenuItem("Tentang Aplikasi", R.drawable.ic_info) { menuOpen = false; onAbout() }
                    MenuItem("Keluar", R.drawable.ic_logout) { menuOpen = false; onExit() }
                }
            }
        }
    }
}

@Composable
private fun MenuItem(label: String, icon: Int, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, fontSize = 14.sp) },
        leadingIcon = {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp))
        },
        onClick = onClick
    )
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = { Text("Cari mahasiswa...", color = HintGrey, fontSize = 14.sp) },
        leadingIcon = {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = "Hapus pencarian",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        shape = CircleShape,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun StudentCard(student: Student, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AvatarGrey)
            ) {
                Icon(
                    painterResource(R.drawable.ic_person),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("NIM: ${student.nim}", fontSize = 12.sp, color = HintGrey)
                Text(student.major, fontSize = 12.sp, color = HintGrey)
            }
            IconButton(onClick = onEdit) {
                Icon(
                    painterResource(R.drawable.ic_edit),
                    contentDescription = "Edit ${student.name}",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = "Hapus ${student.name}",
                    tint = DeleteRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(isSearching: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painterResource(R.drawable.ic_school),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(140.dp)
        )
        Text(
            text = if (isSearching) "Mahasiswa tidak ditemukan" else "Belum ada data mahasiswa",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        if (!isSearching) {
            Text(
                text = "Tekan tombol + untuk menambahkan mahasiswa pertama.",
                fontSize = 13.sp,
                color = HintGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    StudentManagerTheme {
        HomeScreen(sampleStudents, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    StudentManagerTheme {
        HomeScreen(emptyList(), {}, {}, {}, {})
    }
}
