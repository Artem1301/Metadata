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
    const [downloadUrl, setDownloadUrl] = useState(null);
    const [metadata, setMetadata] = useState(null);
    const [loadingMetadataFor, setLoadingMetadataFor] = useState(null);

    const handleFileChange = (event) => {
        setSelectedFiles([...event.target.files]);
    };

    const handleMetadataFileChange = (event) => {
        setMetadataFile(event.target.files[0]);
    };

    const handleUpload = async () => {
        if (selectedFiles.length === 0 || !metadataFile) {
            toast.error("Виберіть JPG і TXT файл!");
            return;
        }

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
                toast.success("Файли оброблені! Можна завантажити.");
            } else {
                toast.error("Помилка завантаження.");
            }
        } catch (error) {
            toast.error("Помилка з'єднання з сервером.");
        }
    };

    const handleViewMetadata = async (file) => {
        setLoadingMetadataFor(file.name);
        const formData = new FormData();
        formData.append("file", file);

        try {
            const response = await fetch("http://localhost:8080/api/readMetadata", {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                const data = await response.json();
                setMetadata({ fileName: file.name, data });
            } else {
                toast.error("Помилка при читанні метаданих.");
            }
        } catch (error) {
            toast.error("Помилка з'єднання.");
        } finally {
            setLoadingMetadataFor(null);
        }
    };

    return (
        <div className="container">
            <h1>Редактор EXIF метаданих в JPG</h1>

            <input type="file" accept="image/jpeg" multiple onChange={handleFileChange} />
            <input type="file" accept=".txt" onChange={handleMetadataFileChange} />

            <button onClick={handleUpload} className="upload-button">
                Завантажити
            </button>

            {downloadUrl && (
                <div className="download-container">
                    <a href={downloadUrl} download="modified_images.zip" className="download-link">
                        Завантажити файли
                    </a>
                </div>
            )}

            {selectedFiles.length > 0 && (
                <div>
                    <h2>Обрані файли:</h2>
                    <ul>
                        {selectedFiles.map((file, index) => (
                            <li key={index}>
                                {file.name}
                                <button
                                    onClick={() => handleViewMetadata(file)}
                                    disabled={loadingMetadataFor === file.name}
                                    className="metadata-button"
                                >
                                    {loadingMetadataFor === file.name ? "Завантаження..." : "Показати метадані"}
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {metadata && (
                <div className="metadata-container">
                    <h2>Метадані ({metadata.fileName}):</h2>
                    <div className="metadata-box">
                        {Object.entries(metadata.data).map(([key, value]) => (
                            <div key={key}><strong>{key}:</strong> {value}</div>
                        ))}
                    </div>
                </div>
            )}

            <ToastContainer />
        </div>
    );
}
