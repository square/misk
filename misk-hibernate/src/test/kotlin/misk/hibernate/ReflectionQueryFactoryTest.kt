package misk.hibernate

import ch.qos.logback.classic.Level
import com.google.common.collect.Iterables.getOnlyElement
import misk.logging.LogCollector
import misk.testing.MiskTest
import misk.testing.MiskTestModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import javax.inject.Inject
import kotlin.test.assertFailsWith

@MiskTest(startService = true)
class ReflectionQueryFactoryTest {
  @MiskTestModule
  val module = MoviesTestModule()

  val maxMaxRows = 40
  val rowCountErrorLimit = 30
  val rowCountWarningLimit = 20
  private val queryFactory = ReflectionQuery.Factory(ReflectionQuery.QueryLimitsConfig(
      maxMaxRows, rowCountErrorLimit, rowCountWarningLimit))

  @Inject @Movies lateinit var transacter: Transacter
  @Inject lateinit var logCollector: LogCollector

  @Test
  fun comparisonOperators() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", LocalDate.of(2018, 1, 2))
    val m3 = NameAndReleaseDate("Rocky 3", LocalDate.of(2018, 1, 3))
    val m4 = NameAndReleaseDate("Rocky 4", LocalDate.of(2018, 1, 4))
    val m5 = NameAndReleaseDate("Rocky 5", LocalDate.of(2018, 1, 5))
    val m98 = NameAndReleaseDate("Rocky 98", null)
    val m99 = NameAndReleaseDate("Rocky 99", null)

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))
      session.save(DbMovie(m3.name, m3.releaseDate))
      session.save(DbMovie(m4.name, m4.releaseDate))
      session.save(DbMovie(m5.name, m5.releaseDate))
      session.save(DbMovie(m98.name, m98.releaseDate))
      session.save(DbMovie(m99.name, m99.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateLessThan(m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m2)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateLessThanOrEqualTo(m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m2, m3)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateEqualTo(m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactly(m3)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateGreaterThanOrEqualTo(m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m3, m4, m5)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateGreaterThan(m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m4, m5)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateNotEqualTo(m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m2, m4, m5)
    }
  }

  /** Comparisons with null always return an empty list. */
  @Test
  fun comparisonWithNull() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", LocalDate.of(2018, 1, 2))
    val m3 = NameAndReleaseDate("Rocky 3", LocalDate.of(2018, 1, 3))
    val m4 = NameAndReleaseDate("Rocky 4", LocalDate.of(2018, 1, 4))
    val m5 = NameAndReleaseDate("Rocky 5", LocalDate.of(2018, 1, 5))
    val m98 = NameAndReleaseDate("Rocky 98", null)
    val m99 = NameAndReleaseDate("Rocky 99", null)

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))
      session.save(DbMovie(m3.name, m3.releaseDate))
      session.save(DbMovie(m4.name, m4.releaseDate))
      session.save(DbMovie(m5.name, m5.releaseDate))
      session.save(DbMovie(m98.name, m98.releaseDate))
      session.save(DbMovie(m99.name, m99.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateLessThan(null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .isEmpty()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateLessThanOrEqualTo(null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .isEmpty()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateEqualTo(null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .isEmpty()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateGreaterThanOrEqualTo(null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .isEmpty()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateGreaterThan(null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .isEmpty()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateNotEqualTo(null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .isEmpty()
    }
  }

  @Test
  fun nullOperators() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", LocalDate.of(2018, 1, 2))
    val m98 = NameAndReleaseDate("Rocky 98", null)
    val m99 = NameAndReleaseDate("Rocky 99", null)

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))
      session.save(DbMovie(m98.name, m98.releaseDate))
      session.save(DbMovie(m99.name, m99.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateIsNull()
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m98, m99)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateIsNotNull()
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m2)
    }
  }

  @Test
  fun inOperator() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", LocalDate.of(2018, 1, 2))
    val m3 = NameAndReleaseDate("Rocky 3", LocalDate.of(2018, 1, 3))

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))
      session.save(DbMovie(m3.name, m3.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateInVararg(m1.releaseDate, m3.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m3)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateInCollection(listOf(m1.releaseDate, m3.releaseDate))
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m3)
    }
  }

  @Test
  fun inOperatorWithNull() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", null)

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateInVararg(m1.releaseDate, null)
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactly(m1)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateInCollection(listOf(m1.releaseDate, null))
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactly(m1)
    }
  }

  @Test
  fun inOperatorWithEmptyList() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))

    transacter.transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateInVararg()
          .allowFullScatter()
          .listAsNameAndReleaseDate(session))
          .isEmpty()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateInCollection(listOf())
          .allowFullScatter()
          .listAsNameAndReleaseDate(session))
          .isEmpty()
    }
  }

  @Test
  fun singleColumnProjection() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", LocalDate.of(2018, 1, 2))
    val m3 = NameAndReleaseDate("Rocky 3", LocalDate.of(2018, 1, 3))

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))
      session.save(DbMovie(m3.name, m3.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateEqualTo(m1.releaseDate)
          .allowFullScatter().allowTableScan()
          .uniqueName(session))
          .isEqualTo(m1.name)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateLessThanOrEqualTo(m2.releaseDate)
          .allowFullScatter().allowTableScan()
          .listAsNames(session))
          .containsExactlyInAnyOrder(m1.name, m2.name)
    }
  }

  @Test
  fun singleColumnProjectionIsEmpty() {
    transacter.transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .uniqueName(session))
          .isNull()

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .listAsNames(session))
          .isEmpty()
    }
  }

  /**
   * This should be multiple tests but it takes a few seconds to insert many rows so we bundle them
   * all into one big test.
   */
  @Test
  fun rowResultCountWarning() {
    // 18 rows is too small for the warning threshold (20).
    transacter.allowCowrites().transaction { session ->
      for (i in 1..18) {
        session.save(DbMovie("Rocky $i", null))
      }
    }
    transacter.transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>().allowTableScan().allowFullScatter()
          .list(session)).hasSize(18)
    }
    assertThat(logCollector.takeMessages(loggerClass = ReflectionQuery::class)).isEmpty()

    // 21 rows logs a warning.
    transacter.allowCowrites().transaction { session ->
      for (i in 19..21) {
        session.save(DbMovie("Rocky $i", null))
      }
    }
    transacter.transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .list(session)).hasSize(21)
    }
    assertThat(getOnlyElement(
        logCollector.takeMessages(loggerClass = ReflectionQuery::class, minLevel = Level.WARN))
    ).startsWith("Unbounded query returned 21 rows.")

    // 31 rows logs an error.
    transacter.allowCowrites().transaction { session ->
      for (i in 22..31) {
        session.save(DbMovie("Rocky $i", null))
      }
    }
    transacter.allowCowrites().transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .list(session)).hasSize(31)
    }
    assertThat(getOnlyElement(
        logCollector.takeMessages(loggerClass = ReflectionQuery::class, minLevel = Level.ERROR))
    ).startsWith("Unbounded query returned 31 rows.")

    // An explicit max row count suppresses the warning.
    transacter.allowCowrites().transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .apply { maxRows = 32 }
          .list(session))
          .hasSize(31)
    }
    assertThat(logCollector.takeMessages(loggerClass = ReflectionQuery::class)).isEmpty()

    // More than 40 rows throws an exception.
    transacter.allowCowrites().transaction { session ->
      for (i in 32..41) {
        session.save(DbMovie("Rocky $i", null))
      }
    }
    assertThat(assertFailsWith<IllegalStateException> {
      transacter.transaction { session ->
        queryFactory.newQuery<OperatorsMovieQuery>()
            .allowFullScatter().allowTableScan()
            .list(session)
      }
    }).hasMessage("query truncated at 41 rows")
  }

  @Test
  fun maxRowCountNotExceeded() {
    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie("Rocky 1", null))
      session.save(DbMovie("Rocky 2", null))
      session.save(DbMovie("Rocky 3", null))
    }

    transacter.transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .apply { maxRows = 4 }
          .listAsNames(session))
          .hasSize(3)
    }
  }

  @Test
  fun maxMaxRowCountEnforced() {
    val query = queryFactory.newQuery<OperatorsMovieQuery>()
    assertThat(assertFailsWith<IllegalArgumentException> {
      query.maxRows = 10_001
    }).hasMessage("out of range: 10001")
  }

  @Test
  fun maxRowCountTruncates() {
    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie("Rocky 1", null))
      session.save(DbMovie("Rocky 2", null))
      session.save(DbMovie("Rocky 3", null))
    }

    // List.
    transacter.transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .apply { maxRows = 2 }
          .allowFullScatter().allowTableScan()
          .list(session)
          .map { it.name })
          .hasSize(2)
    }

    // List projection.
    transacter.transaction { session ->
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .apply { maxRows = 2 }
          .listAsNames(session))
          .hasSize(2)
    }
  }

  @Test
  fun uniqueResultFailsOnTooManyResults() {
    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie("Rocky 1", null))
      session.save(DbMovie("Rocky 2", null))
      session.save(DbMovie("Rocky 3", null))
    }

    // Unique result.
    assertThat(assertFailsWith<IllegalStateException> {
      transacter.transaction { session ->
        queryFactory.newQuery<OperatorsMovieQuery>()
            .allowFullScatter().allowTableScan()
            .uniqueResult(session)
      }
    }).hasMessageContaining("query expected a unique result but was")

    // Unique result projection.
    assertThat(assertFailsWith<IllegalStateException> {
      transacter.transaction { session ->
        queryFactory.newQuery<OperatorsMovieQuery>()
            .allowFullScatter().allowTableScan()
            .uniqueName(session)
      }
    }).hasMessageContaining("query expected a unique result but was")
  }

  @Test
  fun order() {
    transacter.allowCowrites().transaction { session ->
      val jurassicPark = DbMovie("Jurassic Park", LocalDate.of(1993, 6, 9))
      val rocky = DbMovie("Rocky", LocalDate.of(1976, 11, 21))
      val starWars = DbMovie("Star Wars", LocalDate.of(1977, 5, 25))

      val m1 = NameAndReleaseDate(jurassicPark.name, jurassicPark.release_date)
      val m2 = NameAndReleaseDate(rocky.name, rocky.release_date)
      val m3 = NameAndReleaseDate(starWars.name, starWars.release_date)

      session.save(jurassicPark)
      session.save(rocky)
      session.save(starWars)

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateAsc()
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactly(m2, m3, m1)
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateDesc()
          .allowFullScatter().allowTableScan()
          .listAsNameAndReleaseDate(session))
          .containsExactly(m1, m3, m2)
      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateDesc()
          .allowFullScatter().allowTableScan()
          .list(session))
          .containsExactly(jurassicPark, starWars, rocky)
    }
  }

  @Test
  fun deleteFailsWithOrderBy() {
    transacter.transaction { session ->
      session.save(DbMovie("Rocky 1", null))
    }

    assertThat(assertFailsWith<IllegalStateException> {
      transacter.transaction { session ->
        queryFactory.newQuery<OperatorsMovieQuery>()
            .allowFullScatter().allowTableScan()
            .releaseDateAsc().delete(session)
      }
    }).hasMessageContaining("orderBy shouldn't be used for a delete")

    transacter.allowCowrites().transaction { session ->
      val rocky = queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .uniqueName(session)
      assertThat(rocky).isEqualTo("Rocky 1")
    }
  }

  @Test
  fun delete() {
    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie("Jurassic Park", LocalDate.of(1993, 6, 9)))
      session.save(DbMovie("Rocky", LocalDate.of(1976, 11, 21)))
      session.save(DbMovie("Star Wars", LocalDate.of(1977, 5, 25)))
    }

    val deleted = transacter.transaction { session ->
      queryFactory.newQuery<OperatorsMovieQuery>()
          .releaseDateLessThanOrEqualTo(LocalDate.of(1978, 1, 1))
          .allowFullScatter().allowTableScan()
          .delete(session)
    }
    assertThat(deleted).isEqualTo(2)

    transacter.transaction { session ->
      val jurassicPark = queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .uniqueName(session)
      assertThat(jurassicPark).isEqualTo("Jurassic Park")
    }
  }

  @Test
  fun orOperator() {
    val m1 = NameAndReleaseDate("Rocky 1", LocalDate.of(2018, 1, 1))
    val m2 = NameAndReleaseDate("Rocky 2", LocalDate.of(2018, 1, 2))
    val m3 = NameAndReleaseDate("Rocky 3", LocalDate.of(2018, 1, 3))

    transacter.allowCowrites().transaction { session ->
      session.save(DbMovie(m1.name, m1.releaseDate))
      session.save(DbMovie(m2.name, m2.releaseDate))
      session.save(DbMovie(m3.name, m3.releaseDate))

      assertThat(queryFactory.newQuery<OperatorsMovieQuery>()
          .allowFullScatter().allowTableScan()
          .or {
            option { name("Rocky 1") }
            option { name("Rocky 3") }
          }
          .listAsNameAndReleaseDate(session))
          .containsExactlyInAnyOrder(m1, m3)
    }
  }

  @Test
  fun orWithZeroOptionsExplodes() {
    transacter.transaction { session ->
      assertFailsWith<IllegalStateException> {
        queryFactory.newQuery<OperatorsMovieQuery>()
            .or {
            }
            .list(session)
      }
    }
  }

  @Test
  fun orWithEmptyOptionExplodes() {
    transacter.transaction { session ->
      assertFailsWith<IllegalStateException> {
        queryFactory.newQuery<OperatorsMovieQuery>()
            .or {
              option { }
            }
            .list(session)
      }
    }
  }

  @Test
  fun orOperatorFailsOnNonPredicateCall() {
    transacter.transaction { session ->
      assertFailsWith<IllegalStateException> {
        queryFactory.newQuery<OperatorsMovieQuery>()
            .or {
              option { list(session) }
            }
            .list(session)
      }
      assertFailsWith<IllegalStateException> {
        queryFactory.newQuery<OperatorsMovieQuery>()
            .or {
              option { releaseDateAsc() }
            }
            .list(session)
      }
    }
  }

  interface OperatorsMovieQuery : Query<DbMovie> {
    @Constraint(path = "name")
    fun name(name: String): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.LT)
    fun releaseDateLessThan(upperBound: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.LE)
    fun releaseDateLessThanOrEqualTo(upperBound: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.EQ)
    fun releaseDateEqualTo(upperBound: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.GE)
    fun releaseDateGreaterThanOrEqualTo(upperBound: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.GT)
    fun releaseDateGreaterThan(upperBound: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.NE)
    fun releaseDateNotEqualTo(upperBound: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.IN)
    fun releaseDateInVararg(vararg upperBounds: LocalDate?): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.IN)
    fun releaseDateInCollection(upperBounds: Collection<LocalDate?>): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.IS_NOT_NULL)
    fun releaseDateIsNotNull(): OperatorsMovieQuery

    @Constraint(path = "release_date", operator = Operator.IS_NULL)
    fun releaseDateIsNull(): OperatorsMovieQuery

    @Order(path = "release_date")
    fun releaseDateAsc(): OperatorsMovieQuery

    @Order(path = "release_date", asc = false)
    fun releaseDateDesc(): OperatorsMovieQuery

    @Select
    fun listAsNameAndReleaseDate(session: Session): List<NameAndReleaseDate>

    @Select("name")
    fun uniqueName(session: Session): String?

    @Select("name")
    fun listAsNames(session: Session): List<String>
  }
}
