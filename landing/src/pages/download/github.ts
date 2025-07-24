import axios from 'axios';

export default async function getRepoLatestRelease() {
    const response = await axios.get("https://api.github.com/repos/lavafrai/maiapp/releases/latest");
    return {
        url: response.data.html_url,
        version: response.data.name,
        description: response.data.body,
    }
}