package com.github.wreulicke.no.var;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoVarCheckTest {
  @Test
  void replaceLocalVariable_javaLang() {
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(NoVarCheck.class, getClass());
    helper
        .addInputLines(
            "Foo.java",
            """
              public class Foo {
                 public void method() {
                   var noVar = "";
                 }
              }
              """)
        .addOutputLines(
            "Foo.java",
            """
              public class Foo {
                 public void method() {
                   String noVar = "";
                 }
              }
              """)
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  @Test
  void replaceLocalVariable_AddImport() {
    BugCheckerRefactoringTestHelper helper =
        BugCheckerRefactoringTestHelper.newInstance(NoVarCheck.class, getClass());
    helper
        .addInputLines(
            "Foo.java",
            """
                           public class Foo {
                              public void method() {
                                var noVar = java.util.Optional.of("test");
                              }
                           }
                           """)
        .addOutputLines(
            "Foo.java",
            """
                            import java.util.Optional;
                            public class Foo {
                               public void method() {
                                 Optional noVar = java.util.Optional.of("test");
                               }
                            }
                            """)
        .doTest(BugCheckerRefactoringTestHelper.TestMode.TEXT_MATCH);
  }

  // According to JUnit Jupiter, the nested test class should be non-static
  // https://junit.org/junit5/docs/current/user-guide/#writing-tests-nested
  @SuppressWarnings("ClassCanBeStatic")
  @Nested
  class CompilationTest {
    private CompilationTestHelper helper;

    @BeforeEach
    void setup() {
      helper = CompilationTestHelper.newInstance(NoVarCheck.class, getClass());
    }

    @Test
    void testFooMethod() {
      helper
          .addSourceLines(
              "Foo.java",
              """
          public class Foo {
             public void method() {
               // BUG: Diagnostic contains: Should declare with explicit types
               var noVar = "";
             }
          }
          """)
          .doTest();
    }

    @Test
    void testBarMethod() {
      helper
          .addSourceLines(
              "Foo.java",
              """
          public class Foo {
            void bar() {
              String string = "test";
              java.util.Optional optional = java.util.Optional.of("test");
            }
          }
          """)
          .expectNoDiagnostics()
          .doTest();
    }
  }
}
