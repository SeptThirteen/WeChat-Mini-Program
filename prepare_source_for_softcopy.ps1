# prepare_source_for_softcopy.ps1
# 用法：在仓库根目录打开 PowerShell，运行：
#   .\prepare_source_for_softcopy.ps1
# 脚本会根据 SOURCE_FILE_LIST.txt 将列出的文件/目录拷贝到 softcopy_source 目录（保留相对结构）

$PSScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$manifest = Join-Path $PSScriptRoot 'SOURCE_FILE_LIST.txt'
if (-not (Test-Path $manifest)) {
    Write-Error "找不到 $manifest，请确保脚本与 SOURCE_FILE_LIST.txt 在同一目录。"
    exit 1
}
$destRoot = Join-Path $PSScriptRoot 'softcopy_source'
if (Test-Path $destRoot) {
    Write-Host "目标目录已存在，正在删除： $destRoot"
    Remove-Item -Recurse -Force $destRoot
}
New-Item -ItemType Directory -Path $destRoot | Out-Null

Get-Content $manifest | ForEach-Object {
    $rel = $_.Trim()
    if ($rel -eq '' -or $rel.StartsWith('#')) { return }
    $src = Join-Path $PSScriptRoot $rel
    if (-not (Test-Path $src)) {
        Write-Warning "未找到： $rel ，跳过"
        return
    }
    $target = Join-Path $destRoot $rel
    if ((Get-Item $src).PSIsContainer) {
        Write-Host "拷贝目录： $rel"
        # 使用 robocopy 保持目录结构并排除常见构建产物
        $excludeDirs = "dist","unpackage","target","node_modules"
        $excludeDirsArgs = $excludeDirs -join ' '
        # robocopy 参数：/E 递归包括子目录，/NFL /NDL 减少日志
        $robocopyDest = Split-Path $target -Parent
        if (-not (Test-Path $robocopyDest)) { New-Item -ItemType Directory -Path $robocopyDest -Force | Out-Null }
        robocopy $src $target /E /XD dist unpackage target node_modules /NFL /NDL /NJH /NJS | Out-Null
    }
    else {
        Write-Host "拷贝文件： $rel"
        $parent = Split-Path $target -Parent
        if (-not (Test-Path $parent)) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
        Copy-Item $src -Destination $target -Force
    }
}

Write-Host "已完成：源代码已拷贝到 $destRoot"