package st.misa.menzomat.card.data.profile.novisad

import st.misa.menzomat.core.domain.desfire.Aid

/** Where things live on a University of Novi Sad canteen card. Reverse engineered: https://gist.github.com/MikMik1011/0a923f203c2ce98c4b5bf1537de42fe7 */
internal object NoviSadLayout {
    /**
     * TagInfo prints the application id in wire order (0D 00 00); the numeric id is 0x00000D.
     * Both are accepted so the profile matches whichever way the card reports it.
     */
    val applicationIds = listOf(Aid(0x00000D), Aid(0x0D0000))

    const val HOLDER = 0x00
    const val CARD_NUMBER = 0x01
    const val BALANCE = 0x02
    const val TRANSACTION_COUNTER = 0x03
    const val LAST_TRANSACTION = 0x04
    const val DATES = 0x06
    const val STUDY = 0x07
    const val CASH = 0x08
    const val MEDICAL_CHECK = 0x09

    /** File id to expected byte size, used to recognise the layout. File 0x03 is a value file and has no size. */
    val expectedSizes = mapOf(
        0x00 to 256, 0x01 to 15, 0x02 to 11, 0x04 to 6, 0x05 to 6,
        0x06 to 24, 0x07 to 22, 0x08 to 6, 0x09 to 128, 0x0A to 7,
    )

    /**
     * Faculty id (first byte of file 0x07) to the key the app words in each language. Other ids show as
     * "Faculty N". Add the name in both string files when adding one here.
     */
    val facultyKeys: Map<Int, String> = mapOf(
        1 to "uns-ftn", // Faculty of Technical Sciences
        2 to "uns-ff", // Faculty of Philosophy
        4 to "uns-pmf", // Faculty of Sciences
    )
}
