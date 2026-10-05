package com.ute.lab03_bai3
import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ute.lab03_bai3.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "TAG_LIFECYCLE"
    }

    private lateinit var binding: ActivityMainBinding

    // Dữ liệu mẫu sinh viên ban đầu
    private var student = Student(
        id = "2415053122226",
        name = "Nguyễn Quang Nhật",
        className = "24T2",
        email = "2415053122226@sv.ute.udn.vn",
        gpa = 3.8
    )

    // === KHAI BÁO 3 LAUNCHERS CỦA ACTIVITY RESULT API ===
    // 1. Launcher nhận kết quả cập nhật từ EditProfileActivity
    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val updated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                res.data?.getSerializableExtra("UPDATED", Student::class.java)
            } else {
                @Suppress("DEPRECATION")
                res.data?.getSerializableExtra("UPDATED") as? Student
            }

            updated?.let {
                student = it
                bindData(student)
                Toast.makeText(this, "Đã lưu thông tin mới của ${it.name}!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Đã hủy chỉnh sửa!", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Launcher chọn ảnh từ Photo Picker (Không cần xin quyền bộ nhớ nguy hiểm)
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            binding.imgAvatar.setImageURI(it)
            Toast.makeText(this, "Đã thay đổi ảnh đại diện thành công!", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Launcher yêu cầu cấp quyền Camera tại thời điểm chạy (Runtime Permission)
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Đã cấp quyền Camera! Sẵn sàng chụp ảnh.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bạn đã từ chối quyền Camera!", Toast.LENGTH_SHORT).show()
        }
    }

    // === 7 PHƯƠNG THỨC VÒNG ĐỜI ACTIVITY (LAB 1 & LAB RUBRIC) ===
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity đang được khởi tạo và nạp layout")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bindData(student)

        // 1. Nút Sửa thông tin -> Gọi Explicit Intent sang EditProfileActivity
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java).apply {
                putExtra("STUDENT", student)
            }
            editLauncher.launch(intent)
        }

        // 2. Nút Đổi Avatar -> Mở Photo Picker với MIME type image/*
        binding.btnChangeAvatar.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // 3. Nút Gọi Cố vấn -> Implicit Intent ACTION_DIAL
        binding.btnCallHotline.setOnClickListener {
            val phone = "0905123456"
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(dialIntent)
        }

        // 4. Nút Xin quyền Camera
        binding.btnRequestCamera.setOnClickListener {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }

        // 5. Nút Mở rộng: Google Maps chỉ đường đến trường ĐH Sư Phạm Kỹ Thuật (UTE)
        binding.btnOpenMap.setOnClickListener {
            val mapUri = Uri.parse("geo:16.0768,108.2141?q=Đại+học+Sư+phạm+Kỹ+thuật+Đà+Nẵng")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                // Fallback nếu không có Google Maps thì mở qua app bản đồ mặc định
                val fallbackIntent = Intent(Intent.ACTION_VIEW, mapUri)
                startActivity(Intent.createChooser(fallbackIntent, "Chọn ứng dụng bản đồ"))
            }
        }
    }

    private fun bindData(s: Student) {
        binding.tvName.text = s.name
        binding.tvDetails.text = "Mã SV: ${s.id} | Lớp: ${s.className}"
        val rank = if (s.gpa >= 3.6) "Xuất sắc" else if (s.gpa >= 3.2) "Giỏi" else "Khá"
        binding.tvGpaBadge.text = "GPA: ${s.gpa} - $rank"
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: Activity đã hiển thị trên màn hình (chưa nhận tương tác)")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity ở trạng thái đỉnh stack, sẵn sàng tương tác!")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: Activity bị che khuất một phần (mở dialog / chuẩn bị rời đi)")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: Activity bị ẩn hoàn toàn (bấm Home / mở Activity khác)")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: Người dùng mở lại Activity từ trạng thái Stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Activity bị hủy hoàn toàn khỏi bộ nhớ RAM!")
    }
}