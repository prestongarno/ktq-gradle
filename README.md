# ktq-gradle
Generates Kotlin types for the [ktq graphql client](https://github.com/prestongarno/ktq)

## How to use

1. Add the plugin to the buildscript classpath (where your kotlin plugin is)

    `classpath 'com.prestongarno.ktq:ktq-gradle:0.1`
2. If you haven't already, specify either JCenter or maven Central Repository in the buildscript

       repositories {
         maven { mavenCentral() }  // Or jcenter()
       }

3. Apply the plugin

    `apply plugin: 'com.prestongarno.ktq'`

4. Configure the plugin to target & compile your schema as kotlin by inserting a config block titled `ktq` in your `build.gradle`.
See the next section for how to do this


## Configuration

The list below shows all possible options which you can specify in the `ktq` configuration closure. Only the first option is required.

<dl>
  <dt>schema</dt>
  <dd>Insert the relative or fully qualified path to your GraphQl schema. Defaults to '$buildDir/generated/ktq/'</dd>

  <dt>targetDir</dt>
  <dd>The location for the generated source files. <b>NOTE:</b> <i>The plugin does not support JVM/JS compiling, configuring as dependencies, or adding generated source to to your project classpath!</i> See this <a href="https://stackoverflow.com/questions/20700053/how-to-add-local-jar-file-dependency-to-build-gradle-file">StackOverflow answer</a> for an easy solution</dd>

  <dt>packageName</dt>
  <dd>Insert the package name to use for the generated kotlin types</dd>
  
  <dt>kotlinName</dt>
  <dd>Insert the file name to use for the generated file. Otherwise, it will use the schema name (changing it to a JVM-compliant name if not already)</dd>
</dl>

## Example configuration

        ktq {
        
          schema 'src/resources/idl.graphqls'
          
          targetDir 'generated/kotlin/src/'
          
          packageName 'com.example.graphql'
          
          kotlinName 'GraphqlJvmTypes'
        }
