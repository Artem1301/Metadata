import os
import subprocess
from flask import Flask, request, jsonify, send_file
from werkzeug.utils import secure_filename
import zipfile
import io
from flask_cors import CORS

app = Flask(__name__)

# Настройка CORS
CORS(app)

UPLOAD_FOLDER = './uploads'
ALLOWED_EXTENSIONS = {'jpg', 'jpeg', 'txt'}
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER


# Функція для перевірки типу файлу
def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


# Функція для оновлення метаданих через ExifTool
def update_metadata_exiftool(image_path, new_metadata):
    command = ["exiftool", "-overwrite_original"]
    for key, value in new_metadata.items():
        # додаємо параметри командного рядка для ExifTool
        command.append(f"-{key}={value}")
    command.append(image_path)

    try:
        subprocess.run(command, check=True)
        return True
    except subprocess.CalledProcessError as e:
        return {"error": str(e)}


# Ендпоінт для завантаження файлів
@app.route('/api/upload', methods=['POST'])
def upload_files():
    if 'files' not in request.files or 'metadata' not in request.files:
        return jsonify({"error": "No file part"}), 400

    files = request.files.getlist('files')
    metadata_file = request.files['metadata']

    if not files or not metadata_file or not allowed_file(metadata_file.filename):
        return jsonify({"error": "Invalid file format"}), 400

    # Зберігаємо завантажені файли
    saved_files = []
    for file in files:
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(file_path)
            saved_files.append(file_path)

    metadata_file_path = os.path.join(app.config['UPLOAD_FOLDER'], secure_filename(metadata_file.filename))
    metadata_file.save(metadata_file_path)

    # Читаємо метадані з текстового файлу
    with open(metadata_file_path, 'r') as f:
        metadata_lines = f.readlines()

    new_metadata = {}
    for line in metadata_lines:
        parts = line.strip().split(':')
        if len(parts) == 2:
            key = parts[0].strip()
            value = parts[1].strip()
            new_metadata[key] = value

    # Оновлюємо метадані в зображеннях
    for file_path in saved_files:
        update_metadata_exiftool(file_path, new_metadata)

    # Створюємо архів із файлами
    zip_filename = 'modified_images.zip'
    zip_filepath = os.path.join(app.config['UPLOAD_FOLDER'], zip_filename)

    with zipfile.ZipFile(zip_filepath, 'w') as zipf:
        for file_path in saved_files:
            zipf.write(file_path, os.path.basename(file_path))
        zipf.write(metadata_file_path, os.path.basename(metadata_file_path))

    return send_file(zip_filepath, as_attachment=True, download_name=zip_filename)


# Ендпоінт для читання метаданих
@app.route('/api/readMetadata', methods=['POST'])
def read_metadata_endpoint():
    if 'file' not in request.files:
        return jsonify({"error": "No file part"}), 400

    file = request.files['file']

    if not allowed_file(file.filename):
        return jsonify({"error": "Invalid file format"}), 400

    filename = secure_filename(file.filename)
    file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(file_path)

    # Викликаємо ExifTool для читання метаданих
    command = ["exiftool", "-json", file_path]
    try:
        result = subprocess.run(command, capture_output=True, check=True, text=True)
        metadata = result.stdout
        return jsonify({"file_name": filename, "data": metadata})
    except subprocess.CalledProcessError as e:
        return jsonify({"error": f"Error reading metadata: {e}"}), 400


if __name__ == '__main__':
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)
    app.run(debug=True, host='0.0.0.0', port=5000)
