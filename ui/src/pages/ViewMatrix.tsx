import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  TextField,
  FormControl,
  FormLabel,
  FormGroup,
  FormControlLabel,
  Checkbox,
  Button,
} from "@mui/material";
import MatrixCard from "../components/containers/MatrixCard";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { ROUTES } from "../helpers/helpers";

interface CarverMatrix {
  matrixId: number;
  name: string;
  description: string;
  hosts: string[];
  participants: string[];
}

const ViewMatrix: React.FC = () => {
  const [matrices, setMatrices] = useState<CarverMatrix[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();
  const [roleFilters, setRoleFilters] = useState({
    host: false,
    participant: false,
    both: false,
  });
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserEmail = async () => {
      try {
        const response = await axios.get('/api/user2/whoami-upsert', { withCredentials: true });
        if (response.data.includes('{')) {
          const userData = JSON.parse(response.data.split('}{')[0] + '}');
          setUserEmail(userData.email);
        }
      } catch (error) {
        console.error('Error fetching user email:', error);
      }
    };

    const fetchMatrices = async () => {
      const url = "/api/carvermatrices/search";
      try {
        const response = await axios.get(url, { withCredentials: true });
        let matrixData;
        if (response.data.includes("[")) {
          const parts = response.data.split("]{");
          matrixData = JSON.parse(parts[0] + "]");
          setMatrices(matrixData);
        }
      } catch (error) {
        console.error("Error fetching matrices:", error);
      }
    };

    fetchUserEmail();
    fetchMatrices();
  }, []);

  const handleRoleFilterChange = (role: keyof typeof roleFilters) => {
    setRoleFilters(prev => ({
      ...prev,
      [role]: !prev[role]
    }));
  };

  // Matrices can be filtered by name, description, or role
  const filteredMatrices = matrices.filter((matrix) => {
    // Text search filter
    const term = searchTerm.toLowerCase();
    const matchesSearch = matrix.name.toLowerCase().includes(term) ||
      matrix.description.toLowerCase().includes(term);

    // If no role filters are selected, only apply text search
    if (!roleFilters.host && !roleFilters.participant && !roleFilters.both) {
      return matchesSearch;
    }

    // Role-based filtering
    if (!userEmail) {
      console.log('No user email available');
      return false;
    }

    const isHost = matrix.hosts?.includes(userEmail) || false;
    const isParticipant = matrix.participants?.includes(userEmail) || false;
    const isBoth = isHost && isParticipant;

    console.log('Matrix:', matrix.name);
    console.log('Current user email:', userEmail);
    console.log('Hosts:', matrix.hosts);
    console.log('Is host:', isHost);
    console.log('Is participant:', isParticipant);
    console.log('Is both:', isBoth);
    console.log('Role filters:', roleFilters);

    const matchesRole = 
      (roleFilters.host && isHost) ||
      (roleFilters.participant && isParticipant) ||
      (roleFilters.both && isBoth);

    console.log('Matches role:', matchesRole);
    console.log('Matches search:', matchesSearch);
    console.log('Final result:', matchesSearch && matchesRole);

    return matchesSearch && matchesRole;
  });

  return (
    <Box display="flex">
      {/* Sidebar Filters */}
      <Box
        sx={{
          width: "20%",
          padding: 2,
          backgroundColor: "white",
          height: "100vh",
          boxShadow: 3,
          input: { color: "black" },
          display: "flex",
          flexDirection: "column",
          gap: 2,
        }}
      >
        <TextField
          fullWidth
          focused
          variant="outlined"
          label="Search Matrix"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
        {/* Role Filters */}
        <FormControl component="fieldset">
          <FormLabel component="legend">Role</FormLabel>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox
                  checked={roleFilters.host}
                  onChange={() => handleRoleFilterChange('host')}
                />
              }
              label="Host"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={roleFilters.participant}
                  onChange={() => handleRoleFilterChange('participant')}
                />
              }
              label="Participant"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={roleFilters.both}
                  onChange={() => handleRoleFilterChange('both')}
                />
              }
              label="Both"
            />
          </FormGroup>
        </FormControl>

        <Button
          variant="contained"
          color="success"
          onClick={() => navigate(ROUTES.createMatrix)}
          fullWidth
          sx={{ mt: 1 }}
        >
          Create new matrix
        </Button>
      </Box>

      {/* Main Content */}
      <Box
        id="viewMatricesBox"
        sx={{
          padding: 2,
          width: "80%",
          height: "100vh",
          boxShadow: 3,
          display: "flex",
          justifyContent: "flex-start",
          alignItems: "stretch",
          flexDirection: "column",
          overflow: "auto",
        }}
      >
        {filteredMatrices.length > 0 ? (
          filteredMatrices.map((matrix) => (
            <MatrixCard
              key={matrix.matrixId}
              title={matrix.name}
              description={matrix.description}
              onEditMatrix={() => {
                const url = `/EditMatrix?matrixId=${matrix.matrixId}`;
                navigate(url);
              }}
              titleColor="black"
              buttonVariant="contained"
            />
          ))
        ) : (
          <Typography>No matrices found.</Typography>
        )}
      </Box>
    </Box>
  );
};

export default ViewMatrix;
