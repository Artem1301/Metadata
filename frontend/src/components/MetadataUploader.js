import { useState, useEffect } from "react";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import "../styles/App.css";

export default function MetadataUploader() {
    useEffect(() => {
        document.title = "Курсова Демиденко";
    }, []);

    const [selectedFiles, setSelectedFiles] = useState([]);
    const [metadataFile, setMetadataFile] = useState(null);
    const [uploadMessage, setUploadMessage] = useState("");
    const [downloadUrl, setDownloadUrl] = useState(null);
    const [metadata, setMetadata] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleFileChange = (event) => {
        setSelectedFiles([...event.target.files]);
    };

    const handleMetadataFileChange = (event) => {
        setMetadataFile(event.target.files[0]);
    };

    const handleUpload = async () => {
        if (selectedFiles.length === 0 || !metadataFile) {
            toast.error("Выберите JPG и TXT файл!");
            return;
        }

        setLoading(true);
        const formData = new FormData();
        selectedFiles.forEach(file => formData.append("files", file));
        formData.append("metadata", metadataFile);

        try {
            const response = await fetch("http://localhost:8080/api/upload", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                setDownloadUrl(url);
                toast.success("Файлы обработаны! Можно скачать.");
            } else {
                toast.error("Ошибка загрузки.");
            }
        } catch (error) {
            toast.error("Ошибка соединения с сервером.");
        } finally {
            setLoading(false);
        }
    };

    const handleViewMetadata = async (file) => {
        setLoading(true);
        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("http://localhost:8080/api/readMetadata", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                const data = await response.json();
                setMetadata(data);
            } else {
                toast.error("Ошибка при чтении метаданных.");
            }
        } catch (error) {
            toast.error("Ошибка соединения.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <h1>JPG Metadata Editor</h1>

            <input type="file" accept="image/jpeg" multiple onChange={handleFileChange} />
            <input type="file" accept=".txt" onChange={handleMetadataFileChange} />
            <button onClick={handleUpload} disabled={loading}>Загрузить</button>

            {loading && <p>Загрузка...</p>}
            {uploadMessage && <p>{uploadMessage}</p>}
            {downloadUrl && <a href={downloadUrl} download="modified_images.zip">Скачать файлы</a>}

            {selectedFiles.length > 0 && (
                <div>
                    <h2>Выбранные файлы:</h2>
                    <ul>
                        {selectedFiles.map((file, index) => (
                            <li key={index}>
                                {file.name} <button onClick={() => handleViewMetadata(file)}>Показать метаданные</button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {metadata && (
                <div>
                    <h2>Метаданные</h2>
                    <pre>{JSON.stringify(metadata, null, 2)}</pre>
                </div>
            )}

            <ToastContainer />
        </div>
    );
}
