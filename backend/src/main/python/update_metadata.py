import subprocess
import sys

def update_metadata(image_path, output_path):
    subprocess.run([
        "exiftool",
        "-overwrite_original",
        "-Make=MyBrand",
        "-Model=MyModelX100",
        "-DateTimeOriginal=2025:03:08 12:34:56",
        "-FNumber=1.8",
        "-ExposureTime=1/250",
        "-ISO=200",
        "-Caption-Abstract=Фото повного місяця у високій якості.",
        "-Keywords=місяць,астрономія,ніч",
        "-By-line=John Doe",
        "-Copyright=© 2025 John Doe",
        "-City=Київ",
        "-Country=Україна",
        "-XMP-dc:Title=Повний місяць 2025",
        "-XMP-dc:Creator=John Doe",
        "-XMP-dc:Rights=© 2025 John Doe",
        "-XMP-dc:Subject=астрономія,місяць,нічне небо",
        "-XMP-xmp:ModifyDate=2025:03:08T12:34:56",
        "-o", output_path,  # Сохранить в новый файл
        image_path
    ])

if __name__ == "__main__":
    image_path = sys.argv[1]
    output_path = sys.argv[2]
    update_metadata(image_path, output_path)
    print("✅ Метаданные обновлены:", output_path)
