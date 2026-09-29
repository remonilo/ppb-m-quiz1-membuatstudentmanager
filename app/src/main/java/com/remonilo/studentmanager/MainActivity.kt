package com.remonilo.studentmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.remonilo.studentmanager.ui.theme.StudentManagerTheme


// Warna sesuai desain
internal val HeaderGrey = Color(0xFFE1E5EC)
internal val BluePrimary = Color(0xFF1976F2)
private val SelectedItem = Color(0xFFE3EEFC)
private val HintGrey = Color(0xFF8A8F98)

private val majors = listOf(
    "Informatika",
    "Sistem Informasi",
    "Teknik Komputer",
    "Desain Komunikasi Visual",
    "Manajemen",
    "Akuntansi"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentManagerTheme {
                StudentManagerApp(onExit = { finish() })
            }
        }
    }
}

private val studentsSaver = listSaver<List<Student>, String>(
    save = { list -> list.flatMap { listOf(it.nim, it.name, it.major) } },
    restore = { flat -> flat.chunked(3).map { (nim, name, major) -> Student(nim, name, major) } }
)

@Composable
fun StudentManagerApp(onExit: () -> Unit = {}) {
    var students by rememberSaveable(stateSaver = studentsSaver) { mutableStateOf(sampleStudents) }
    var showAddForm by rememberSaveable { mutableStateOf(false) }
    var editingNim by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = students.firstOrNull { it.nim == editingNim }
    var deletingNim by rememberSaveable { mutableStateOf<String?>(null) }
    val deleting = students.firstOrNull { it.nim == deletingNim }

    when {
        showAddForm -> {
            BackHandler { showAddForm = false }
            AddStudentScreen(
                existingNims = students.map { it.nim }.toSet(),
                onBack = { showAddForm = false },
                onSave = { student ->
                    students = students + student
                    showAddForm = false
                }
            )
        }

        editing != null -> {
            BackHandler { editingNim = null }
            EditStudentScreen(
                student = editing,
                existingNims = students.map { it.nim }.toSet() - editing.nim,
                onBack = { editingNim = null },
                onSave = { updated ->
                    students = students.map { if (it.nim == editing.nim) updated else it }
                    editingNim = null
                }
            )
        }

        else -> {
            HomeScreen(
                students = students,
                onAddClick = { showAddForm = true },
                onEditClick = { editingNim = it.nim },
                onDeleteClick = { deletingNim = it.nim },
                onExit = onExit
            )
            if (deleting != null) {
                ShowDeleteConfirmation(
                    onDismiss = { deletingNim = null },
                    onConfirm = {
                        students = students.filterNot { it.nim == deleting.nim }
                        deletingNim = null
                    }
                )
            }
        }
    }
}

@Composable
fun AddStudentScreen(
    existingNims: Set<String> = emptySet(),
    onBack: () -> Unit = {},
    onSave: (Student) -> Unit = {}
) {
    StudentFormScreen(
        title = "Tambah Mahasiswa",
        initial = Student("", "", ""),
        existingNims = existingNims,
        onBack = onBack,
        onSave = onSave
    )
}

@Composable
fun EditStudentScreen(
    student: Student,
    existingNims: Set<String> = emptySet(),
    onBack: () -> Unit = {},
    onSave: (Student) -> Unit = {}
) {
    StudentFormScreen(
        title = "Edit Mahasiswa",
        initial = student,
        existingNims = existingNims,
        onBack = onBack,
        onSave = onSave
    )
}

@Composable
fun ShowDeleteConfirmation(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Hapus Mahasiswa?", fontWeight = FontWeight.Bold) },
        text = { Text("Apakah Anda yakin ingin menghapus data mahasiswa ini?") },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) { Text("Batal") }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE03030))
            ) { Text("Hapus", color = Color.White) }
        }
    )
}

@Composable
private fun StudentFormScreen(
    title: String,
    initial: Student,
    existingNims: Set<String>,
    onBack: () -> Unit,
    onSave: (Student) -> Unit
) {
    var studentNIM by rememberSaveable { mutableStateOf(initial.nim) }
    var studentName by rememberSaveable { mutableStateOf(initial.name) }
    var studentMajor by rememberSaveable { mutableStateOf(initial.major) }

    val nimTaken = studentNIM.trim() in existingNims
    val canSave = studentNIM.isNotBlank() && studentName.isNotBlank() &&
        studentMajor.isNotBlank() && !nimTaken

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        FormTopBar(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabeledTextField(
                label = "NIM",
                value = studentNIM,
                onValueChange = { studentNIM = it },
                placeholder = "Masukkan NIM",
                errorText = if (nimTaken) "NIM sudah terdaftar" else null
            )
            LabeledTextField(
                label = "Nama",
                value = studentName,
                onValueChange = { studentName = it },
                placeholder = "Masukkan nama mahasiswa"
            )
            MajorDropdown(
                selected = studentMajor,
                onSelected = { studentMajor = it }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                modifier = Modifier.weight(1f)
            ) { Text("Batal") }
            Button(
                onClick = { onSave(Student(studentNIM.trim(), studentName.trim(), studentMajor)) },
                enabled = canSave,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier.weight(1f)
            ) { Text("Simpan") }
        }
    }
}

@Composable
private fun FormTopBar(title: String, onBack: () -> Unit) {
    Surface(color = HeaderGrey, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Kembali")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorText: String? = null
) {
    Column {
        Text(text = label, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = HintGrey) },
            singleLine = true,
            isError = errorText != null,
            supportingText = errorText?.let { { Text(it) } },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MajorDropdown(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = "Program Studi", fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Pilih program studi", color = HintGrey) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color.White
            ) {
                majors.forEach { major ->
                    DropdownMenuItem(
                        text = { Text(major, fontSize = 14.sp) },
                        onClick = {
                            onSelected(major)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (major == selected) SelectedItem else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentManagerPreview() {
    StudentManagerTheme {
        StudentManagerApp()
    }
}
