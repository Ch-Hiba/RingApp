import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ringapp.Contact
import com.example.ringapp.R

class ContactAdapter(private val context: Context) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_item, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact)
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profile: ImageView = itemView.findViewById(R.id.contactImage)
        private val name: TextView = itemView.findViewById(R.id.contactName)
        private val phone: TextView = itemView.findViewById(R.id.PhoneNumber)
        private val moreOptionsImageView: ImageView = itemView.findViewById(R.id.iconMenu)

        fun bind(contact: Contact) {
            name.text = contact.name
            phone.text = contact.phoneNumber

            displayProfileImage(contact)

            moreOptionsImageView.setOnClickListener {
                val popupMenu = PopupMenu(context, moreOptionsImageView)
                popupMenu.inflate(R.menu.menu)
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_send_message -> {
                            openMessagingApp(contact.phoneNumber)
                            true
                        }
                        R.id.action_call -> {
                            makeCall(contact.phoneNumber)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }
        }

        private fun displayProfileImage(contact: Contact) {
            if (contact.name.isNullOrEmpty()) {
                profile.setImageResource(R.drawable.baseline_person_24)
            } else {
                val firstLetter = contact.name.first().toString()
                val letterBitmap = createLetterBitmap(firstLetter)
                profile.setImageBitmap(letterBitmap)
            }
        }

        private fun createLetterBitmap(letter: String): Bitmap {
            val size = 100
            val backgroundColor = Color.rgb(
                Math.abs(letter.hashCode() * 73 % 255),
                Math.abs(letter.hashCode() * 37 % 255),
                Math.abs(letter.hashCode() * 53 % 255)
            )

            val paint = Paint().apply {
                textSize = 50f
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val backgroundPaint = Paint().apply {
                color = backgroundColor
                isAntiAlias = true
            }
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint)
            canvas.drawText(letter, size / 2f, size / 2f - (paint.ascent() + paint.descent()) / 2, paint)

            return bitmap
        }

        private fun openMessagingApp(phoneNumber: String) {
            val messageIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber"))
            try {
                context.startActivity(messageIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur : Impossible d'ouvrir l'application de messagerie", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        private fun makeCall(phoneNumber: String) {
            try {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:$phoneNumber")
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to make the call", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem.phoneNumber == newItem.phoneNumber
    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem == newItem
}
