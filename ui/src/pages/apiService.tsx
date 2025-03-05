import axios from 'axios';

const api = axios.create({
  baseURL: 'https://starter-app.zeus.socom.dev/api',
});

// Function for the "Whoami Upsert" call
export const whoamiUpsert = async (): Promise<any> => {
    return api.get('/user2/whoami-upsert');
  };
  
  // Function to call the Create Matrix endpoint
  export const createMatrix = async (matrixData: any, userId: string): Promise<any> => {
    return api.post(`/carvermatrices/create?userId=${userId}`, matrixData);
  };
