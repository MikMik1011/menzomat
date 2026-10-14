package st.misa.menzomat.fake

import okio.FileSystem
import okio.Path
import kotlin.random.Random

/** A path in a fresh temporary directory, for DataStore files in tests. */
fun tempFilePath(name: String): Path {
    val dir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "menzomat-test-${Random.nextLong().toULong()}"
    FileSystem.SYSTEM.createDirectories(dir)
    return dir / name
}
