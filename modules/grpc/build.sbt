import sbt.Keys._
import sbtprotoc.ProtocPlugin.autoImport.PB

PB.targets in Compile := Seq(
  scalapb.gen(flatPackage = true, singleLineToString = true) -> (sourceManaged in Compile).value
)
//PB.targets in Compile := Seq(
//  scalapb.gen(flatPackage = true, singleLineToString = true) -> ((sourceDirectory in Compile).value / "generated")
//)
//unmanagedSourceDirectories in Compile += (sourceDirectory in Compile).value / "generated"
//cleanFiles <+= baseDirectory { base => base / "src" / "main" / "generated" }
//compile in Compile := (compile in Compile).dependsOn(clean).value
