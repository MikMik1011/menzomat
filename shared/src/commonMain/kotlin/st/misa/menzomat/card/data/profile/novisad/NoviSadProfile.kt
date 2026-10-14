package st.misa.menzomat.card.data.profile.novisad

import st.misa.menzomat.card.domain.model.ExtraField
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import st.misa.menzomat.card.domain.model.Holder
import st.misa.menzomat.card.domain.model.Institution
import st.misa.menzomat.card.domain.model.MealBalance
import st.misa.menzomat.card.domain.model.StudentCard
import st.misa.menzomat.card.domain.model.Transaction
import st.misa.menzomat.card.domain.model.Validity
import st.misa.menzomat.card.domain.profile.CardProfile
import st.misa.menzomat.card.domain.profile.DecodeScope
import st.misa.menzomat.core.domain.desfire.AppDump
import st.misa.menzomat.core.domain.desfire.CardDump
import st.misa.menzomat.core.domain.desfire.FileType
import st.misa.menzomat.core.domain.desfire.RawFile
import st.misa.menzomat.core.domain.util.ByteCursor
import st.misa.menzomat.core.domain.util.MonthBase
import st.misa.menzomat.core.domain.util.hex
import st.misa.menzomat.core.domain.util.dateOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.YearMonth

/** Student canteen card of the University of Novi Sad (DESFire application 0x0D0000, free read). */
internal class NoviSadProfile : CardProfile {
    override val id = "uns"
    override val displayName = "Univerzitet u Novom Sadu"
    override val shortName = "UNS"

    override fun score(dump: CardDump): Int {
        val app = findApplication(dump) ?: return 0
        val sizedFiles = NoviSadLayout.expectedSizes.count { (fileId, size) -> app.file(fileId)?.byteSize == size }
        val counter = if (app.file(NoviSadLayout.TRANSACTION_COUNTER)?.type == FileType.Value) 1 else 0
        return BASE_SCORE + (sizedFiles + counter) * PER_FILE_SCORE
    }

    override fun decode(dump: CardDump): StudentCard {
        val app = findApplication(dump)
        val scope = DecodeScope()
        val today = Instant.fromEpochMilliseconds(dump.readAtEpochMillis).toLocalDateTime(CARD_ZONE).date

        val holder = scope.readFile(app, NoviSadLayout.HOLDER) { data ->
            val cursor = ByteCursor(data)
            Holder(cursor.lengthPrefixedText(), cursor.lengthPrefixedText(), cursor.lengthPrefixedText())
        }

        val cardFile = scope.readFile(app, NoviSadLayout.CARD_NUMBER) { data ->
            val cursor = ByteCursor(data)
            cursor.skip(2)
            val date = Triple(cursor.u8(), cursor.u8(), cursor.u8())
            cursor.skip(1)
            CardNumberFile(date, cursor.bytes(cursor.remaining).takeWhile { it != 0.toByte() }
                .joinToString("") { it.toInt().toChar().toString() })
        }

        val balances = scope.readFile(app, NoviSadLayout.BALANCE) { data ->
            val cursor = ByteCursor(data)
            val year = cursor.u8()
            val month = cursor.u8()
            val yearMonth = runCatching { YearMonth(2000 + year, month) }.getOrNull()
            if (yearMonth == null) scope.warn(fileLabel(NoviSadLayout.BALANCE), "invalid balance month $month/$year")
            Balances(month, yearMonth, cursor.meals(), cursor.meals(), cursor.meals())
        }

        val transaction = scope.readFile(app, NoviSadLayout.LAST_TRANSACTION) { data ->
            val cursor = ByteCursor(data)
            val day = cursor.u8()
            val month = cursor.u8()
            val year = cursor.u8()
            val meals = cursor.meals()
            val date = resolveTransactionDate(day, month, year, balances?.monthValue, today)
            if (date == null) scope.warn(fileLabel(NoviSadLayout.LAST_TRANSACTION), "invalid transaction date $day.$month.$year")
            Transaction(date, meals)
        }

        val validityFile = scope.readFile(app, NoviSadLayout.DATES) { data ->
            val cursor = ByteCursor(data)
            val issued = cursor.date(MonthBase.OneBased)
            cursor.skip(3)
            cursor.skip(1)
            val validated = cursor.date(MonthBase.OneBased)
            Pair(issued, validated)
        }

        val studentFile = scope.readFile(app, NoviSadLayout.STUDY) { data ->
            val cursor = ByteCursor(data)
            val faculty = cursor.u8()
            val year = cursor.u8()
            StudentFile(faculty, year, cursor.lengthPrefixedText())
        }

        val cash = scope.readFile(app, NoviSadLayout.CASH) { data -> ByteCursor(data).u24be() }

        val medical = scope.readFile(app, NoviSadLayout.MEDICAL_CHECK) { data ->
            val cursor = ByteCursor(data)
            val date = cursor.date(MonthBase.OneBased)
            val done = cursor.u8() == 1
            if (done) date else null
        }

        val validatedUntil = validityFile?.second
        val validity = if (validityFile != null || cardFile != null) {
            Validity(
                issuedOn = validityFile?.first,
                cardValidUntil = cardFile?.let { resolveCardDate(it.date, validatedUntil) },
                validatedUntil = validatedUntil,
            )
        } else {
            null
        }

        val extras = buildList {
            if (app?.file(NoviSadLayout.MEDICAL_CHECK)?.data != null) {
                add(
                    ExtraField(
                        key = "medical-check",
                        label = "Poslednji sistematski pregled",
                        value = medical?.let(DISPLAY_DATE::format) ?: "Nije urađen",
                    ),
                )
            }
            app?.file(NoviSadLayout.TRANSACTION_COUNTER)?.data?.let { data ->
                runCatching { ByteCursor(data).u32le() }.getOrNull()?.let {
                    add(ExtraField("transaction-counter", "Brojač transakcija", it.toString()))
                }
            }
        }

        return StudentCard(
            holder = holder,
            cardNumber = cardFile?.number?.takeIf { it.isNotEmpty() },
            institution = Institution(
                name = displayName,
                faculty = studentFile?.let { facultyName(it.facultyId) },
                studyYear = studentFile?.studyYear,
                studentId = studentFile?.studentId?.takeIf { it.isNotEmpty() },
            ),
            validity = validity,
            balanceMonth = balances?.yearMonth,
            meals = balances?.now,
            mealsLeftToBuy = balances?.leftToBuy,
            nextMonthMeals = balances?.nextMonth,
            cashBalanceRsd = cash,
            lastTransaction = transaction,
            extras = extras,
            warnings = scope.warnings,
        )
    }

    private fun findApplication(dump: CardDump): AppDump? =
        NoviSadLayout.applicationIds.firstNotNullOfOrNull { dump.app(it) }

    private val RawFile.byteSize: Int? get() = size ?: data?.size

    private fun <T> DecodeScope.readFile(app: AppDump?, fileId: Int, block: (ByteArray) -> T?): T? {
        val label = fileLabel(fileId)
        val file = app?.file(fileId)
        val data = file?.data
        if (data == null) {
            if (app != null) warn(label, file?.error ?: "file not present")
            return null
        }
        return field(label) { block(data) }
    }

    private fun ByteCursor.meals() = MealBalance(breakfast = u8(), lunch = u8(), dinner = u8())

    /** `DD MM YY`; the all-zero and 01 01 00 patterns mean "not set". */
    private fun ByteCursor.date(base: MonthBase): LocalDate? {
        val day = u8()
        val month = u8()
        val year = u8()
        if (day == 0 && month == 0 && year == 0) return null
        if (day == 1 && month == 1 && year == 0) return null
        return dateOrNull(day, month, year, base)
    }

    /**
     * Terminals before 2026 stored the month of the last transaction zero based, newer ones one based.
     * Both rewrite the balance record (always one based) in the same step, so it disambiguates; a date
     * that would lie after the read time is never the right reading.
     */
    private fun resolveTransactionDate(day: Int, month: Int, year: Int, balanceMonth: Int?, today: LocalDate): LocalDate? {
        val oneBased = dateOrNull(day, month, year, MonthBase.OneBased)
        val zeroBased = dateOrNull(day, month, year, MonthBase.ZeroBased)
        val preferZeroBased = balanceMonth != null && month == balanceMonth - 1
        val ordered = if (preferZeroBased) listOf(zeroBased, oneBased) else listOf(oneBased, zeroBased)
        return ordered.firstOrNull { it != null && it <= today } ?: ordered.firstOrNull { it != null }
    }

    /** The card-number record has always been written zero based; validity (file 0x06) settles it when set. */
    private fun resolveCardDate(raw: Triple<Int, Int, Int>, validatedUntil: LocalDate?): LocalDate? {
        val (day, month, year) = raw
        val oneBased = dateOrNull(day, month, year, MonthBase.OneBased)
        val zeroBased = dateOrNull(day, month, year, MonthBase.ZeroBased)
        return when {
            validatedUntil != null && oneBased == validatedUntil -> oneBased
            validatedUntil != null && zeroBased == validatedUntil -> zeroBased
            else -> oneBased ?: zeroBased
        }
    }

    private fun facultyName(id: Int): String = NoviSadLayout.facultyNames[id] ?: "Fakultet $id"

    private fun fileLabel(fileId: Int): String = "0x${fileId.hex(2)}"

    private class CardNumberFile(val date: Triple<Int, Int, Int>, val number: String)

    private class Balances(
        val monthValue: Int,
        val yearMonth: YearMonth?,
        val now: MealBalance,
        val leftToBuy: MealBalance,
        val nextMonth: MealBalance,
    )

    private class StudentFile(val facultyId: Int, val studyYear: Int, val studentId: String)

    private companion object {
        const val BASE_SCORE = 45
        const val PER_FILE_SCORE = 5

        val CARD_ZONE: TimeZone = TimeZone.of("Europe/Belgrade")
        val DISPLAY_DATE: DateTimeFormat<LocalDate> = LocalDate.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
            char('.')
        }
    }
}
