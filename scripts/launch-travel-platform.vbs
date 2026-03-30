Set shell = CreateObject("WScript.Shell")
templateRoot = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
templateRoot = CreateObject("Scripting.FileSystemObject").GetParentFolderName(templateRoot)

backendScript = """" & templateRoot & "\backend\scripts\start-backend.ps1"" -RepositoryMode database -BackendPort 19095"
frontendScript = """" & templateRoot & "\scripts\launch-travel-platform.ps1"" -BackendPort 19095 -FrontendPort 19173"

shell.Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -File " & backendScript, 0, False
shell.Run "powershell.exe -NoProfile -ExecutionPolicy Bypass -File " & frontendScript, 0, False
