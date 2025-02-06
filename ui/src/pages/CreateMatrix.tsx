import { Box, Button, FormControl, FormControlLabel, FormLabel, MenuItem, Paper, Select, SelectChangeEvent, Switch, Typography, Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody, 
  TextField } from "@mui/material";
import { useState } from "react";
export const CreateMatrix: React.FC = () => {
  
  const [RoleBasedChecked, setRoleBasedChecked] = useState(true);
  const [AnonymousEntryChecked, setAnonymousEntryChecked] = useState(false);
  const [value, setValue] = useState<number>(5);
  const [randomAssigned, setRandomAssigned] = useState<string>("random");

  const initialMultipliers = {
    Criticality: 1.0,
    Vulnerability: 1.0,
    Accessibility: 1.0,
    Effect: 1.0,
    Recoverability: 1.0,
    Recognizability: 1.0,
  };

  const [multipliers, setMultipliers] = useState(initialMultipliers);

  const carverOrder = [
    { key: "Criticality", header: "C" },
    { key: "Accessibility", header: "A" },
    { key: "Recoverability", header: "R" },
    { key: "Vulnerability", header: "V" },
    { key: "Effect", header: "E" },
    { key: "Recognizability", header: "R" },
  ];

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("")

  const handleSave = () => {
    console.log("Matrix saved:", title);
    //  save logic here.
  };

  // Options for the dropdown for Global Category Multipliers
  const options = [0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0];

  const [roles, setRoles] = useState(["Role Type", "Role Type", "Role Type", "Role Type", "Role Type"]);

  // Function to handle dropdown changes
  const handleUserRoleChange = (index: number, event: SelectChangeEvent<string>) => {
    const newRoles = [...roles];
    newRoles[index] = event.target.value;
    setRoles(newRoles);
  };

  const multipliersHandleChange = (label: string) => (event: SelectChangeEvent<unknown>) => {
    const val = event.target.value as number;
    setMultipliers((prev) => ({
      ...prev,
      [label]: val,
    }));
  };
  
  const roleBasedHandleToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRoleBasedChecked(event.target.checked);
  };

  const anonymousEntryHandleToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    setAnonymousEntryChecked(event.target.checked);
  };

  const scoreRangeHandleChange = (event: SelectChangeEvent<number>) => {
    setValue(event.target.value as number);
  };

  const dataEntryHandleChange = (event: SelectChangeEvent<string>) => {
    setRandomAssigned(event.target.value as string);
  };

  return (
    // This Box is for the whole screen (background); where every other box will be built on.
    <Box
      sx={{
        backgroundColor: 'lightgray',
        height: '100vh',
        width: '100vw',
        display: 'flex',
        justifyContent: 'left',
        alignItems: 'flex-start',
        p: 2
      }}
    >
      {/* This Box is the Left-Half of the screen and it deals with filling the Matrix Parameters and Global Category Multipliers*/}
      <Box
        id='matrixCreatorBox'
        sx={{
          backgroundColor: 'white',
          p: 2,
          borderRadius: 2,
          width: '50%',
          height: '85%',
          boxShadow: 3,
          position: 'center',
          top: '10px',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column',
          overflow: 'auto'
        }}
      >
        {/* This Box is for the Title and Save Button */}
        <Box display="flex" alignItems="center">
        <TextField
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          variant="standard"
          placeholder="Enter your matrix Title..."
          InputProps={{
            disableUnderline: false,
            style: { fontSize: "3rem", color: 'black'},
          }}
          sx={{ flexGrow: 1 }}
        />
        {/* The Save button with extra spacing */}
        <Button
          variant="contained"
          onClick={handleSave}
          sx={{ ml: 10, borderRadius: "20px", width: "100px" }}
        >
          Save
        </Button>
      </Box>

        <TextField
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          multiline
          rows={1}
          variant="outlined"
          fullWidth
          placeholder="Enter your matrix description..."
          InputProps={{
            disableUnderline: true,
            style: { fontSize: "1rem", color: 'black'},
          }}
          sx={{ flexGrow: 1 }}
        />
      
        {/* This Box is for the Matrix Parameters component */}
        <Box
        id='matrixParametersBox'
        sx={{
          backgroundColor: 'white',
          width: '100%',
          height: '100%',
          position: 'center',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column',
          marginBottom: "10px",
          marginTop: "20px"
        }}
        >
        <Typography variant="h4">Matrix Parameters</Typography>
        {/* Flex container to hold two columns side by side */}
        <Box
          sx={{
            display: "flex",
            flexDirection: "row",
            alignItems: "flex-start",
            gap: 10,
            mt: 2,
          }}
        >
          {/* Left Column: Score Range & Role Based Matrix */}
          <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
            {/* Score Range Label */}
            <FormControl sx={{ width: "100%" }}>
              <FormLabel id="score-range-label">Score Range</FormLabel>
              <Select
                labelId="score-range-label"
                id="score-range-select"
                value={value}
                onChange={scoreRangeHandleChange}
                label="Score Range"
                sx={{
                  "& .MuiInputBase-input": {
                    color: "black", // Text color inside the Select box
                  },
                  "& .MuiSelect-icon": {
                    color: "black",
                  },
                  border: "1px solid lightgray",
                  borderRadius: "20px",
                  marginBottom: "10px",
                }}
              >
                <MenuItem value={5}>5-Point Scoring</MenuItem>
                <MenuItem value={10}>10-Point Scoring</MenuItem>
              </Select>
            </FormControl>

            {/* Role-Based Matrix */}
            <Typography>Role-Based Matrix</Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={RoleBasedChecked}
                  onChange={roleBasedHandleToggle}
                  sx={{
                    "& .MuiSwitch-track": {
                      borderRadius: "20px",
                      backgroundColor: RoleBasedChecked ? "blue" : "red",
                      opacity: 1,
                    },
                  }}
                />
              }
              label={RoleBasedChecked ? "Enabled" : "Disabled"}
              sx={{ marginBottom: "10px" }}
            />
          </Box>

          {/* Right Column: Data Entry Assignment Method & Anonymous Entry */}
          <Box sx={{ flex: 1, display: "flex", flexDirection: "column" }}>
            {/* Data Entry Assignment Method Label */}
            <FormControl sx={{ width: "120%" }}>
              <FormLabel id="data-entry-label">
                Data Entry Assignment Method
              </FormLabel>
              <Select
                labelId="data-entry-label"
                id="data-entry-select"
                value={randomAssigned}
                onChange={dataEntryHandleChange}
                label="Data Entry"
                sx={{
                  "& .MuiInputBase-input": {
                    color: "black", // Text color inside the Select box
                  },
                  "& .MuiSelect-icon": {
                    color: "black",
                  },
                  border: "1px solid lightgray",
                  borderRadius: "20px",
                  marginBottom: "10px",
                }}
              >
                <MenuItem value={"random"}>Random</MenuItem>
                <MenuItem value={"assigned"}>Assigned</MenuItem>
              </Select>
            </FormControl>

            {/* Anonymous Entry */}
            <Typography>Anonymous Entry</Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={AnonymousEntryChecked}
                  onChange={anonymousEntryHandleToggle}
                  sx={{
                    "& .MuiSwitch-track": {
                      borderRadius: "20px",
                      backgroundColor: AnonymousEntryChecked ? "blue" : "red",
                      opacity: 1,
                    },
                  }}
                />
              }
              label={AnonymousEntryChecked ? "Enabled" : "Disabled"}
            />
          </Box>
        </Box>
      </Box>

        {/* This Box is for the Global Category Multipliers */}
        <Box
        id='GlobalCategoryMultipliersBox'
        sx={{
          backgroundColor: 'white',
          width: '80%',
          height: '85%',
          position: 'center',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
        >
        <Typography variant="h4">Global Category Multipliers</Typography>
      <Table>
        <TableHead>
          <TableRow>
            {carverOrder.map((item) => (
              <TableCell key={item.key} align="center" sx={{ color: "black", fontWeight: "bold" }}>
                {item.header}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            {carverOrder.map((item) => (
              <TableCell key={item.key} align="center">
                <Select
                  value={
                    multipliers[item.key as keyof typeof initialMultipliers]
                  }
                  onChange={multipliersHandleChange(item.key)}
                  size="small"
                  sx={{
                    "& .MuiInputBase-input": { color: "black" },
                    "& .MuiSelect-icon": { color: "black" },
                    border: "1px solid lightgray",
                    borderRadius: "20px",
                  }}
                >
                  {options.map((option) => (
                    <MenuItem key={option} value={option}>
                      {option.toFixed(1)}x
                    </MenuItem>
                  ))}
                </Select>
              </TableCell>
            ))}
          </TableRow>
        </TableBody>
      </Table>
    </Box>
      </Box>

      {/* Space between the Boxes */}
      <Box sx={{ width: '20px' }} /> 

      {/* This Box is the Right-Half of the screen and it deals with managing participants */}
      <Box
        id="manageParticipantsBox"
        sx={{
          backgroundColor: "white",
          p: 2,
          borderRadius: 2,
          width: "50%",
          height: "85%",
          boxShadow: 3,
          position: "center",
          top: "10px",
          display: "flex",
          alignItems: "flex-start",
          flexDirection: "column",
          overflow: 'auto'
        }}
      >
        {/* Container for Typography and Button */}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between", // Spreads items to opposite sides
            width: "50%",
            marginBottom: "40px",
          }}
        >
          <Typography variant="h4">Manage Participants</Typography>
          <Button variant="contained" sx={{ borderRadius: "20px", width: "100px" }}>
            Invite
          </Button>
        </Box>

        {/* Participant Rows */}
        {roles.map((role, index) => (
          <Paper
            key={index}
            elevation={0} // Remove shadow
            sx={{
              color: "black",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              p: 2,
              gap: 4,
              border: "1px solid #ccc",
              borderRadius: "20px",
              backgroundColor: "white",
              mb: 1
            }}
          >
            <Typography variant="body1">Example User</Typography>
            <Select
              value={role}
              onChange={(event) => handleUserRoleChange(index, event)}
              sx={{
                color: "gray",
                backgroundColor: "white",
                height: "40px",
              }}
            >
              <MenuItem value="Role Type">Role Type</MenuItem>
              <MenuItem value="Admin">Admin</MenuItem>
              <MenuItem value="Editor">Editor</MenuItem>
              <MenuItem value="Viewer">Viewer</MenuItem>
            </Select>
          </Paper>
        ))}
      </Box>
    </Box>
  );
};

export default CreateMatrix;