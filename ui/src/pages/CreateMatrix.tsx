import { Box, FormControl, FormControlLabel, InputLabel, MenuItem, Select, SelectChangeEvent, Switch, Typography } from "@mui/material";
import { useState } from "react";
export const CreateMatrix: React.FC = () => {
  
  const [RoleBasedChecked, setRoleBasedChecked] = useState(true);
  const [AnonymousEntryChecked, setAnonymousEntryChecked] = useState(false);
  const [value, setValue] = useState<number>(5);


  const roleBasedHandleToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRoleBasedChecked(event.target.checked);
  };

  const anonymousEntryHandleToggle = (event: React.ChangeEvent<HTMLInputElement>) => {
    setAnonymousEntryChecked(event.target.checked);
  };

  const scoreRangeHandleChange = (event: SelectChangeEvent<number>) => {
    setValue(event.target.value as number);
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
          flexDirection: 'column'
        }}
      >
        <Typography variant="h2">Example Matrix Title</Typography>
        <Typography variant="body1">Example Matrix Description...</Typography>
        
        {/* Space of 50px (INEFFICIENT, CONSIDER USING STACK COMPONENT INSTEAD) */}
        <Box sx={{ height: '50px' }}></Box>
        
        <Typography variant="h4">Matrix Parameters</Typography>

        {/* Space of 50px (INEFFICIENT, CONSIDER USING STACK COMPONENT INSTEAD) */}
        <Box sx={{ height: '50px' }}></Box>

        {/* This Box is for the Matrix Parameters */}
        <Box
        id='matrixParametersBox'
        sx={{
          backgroundColor: 'white',
          width: '100%',
          height: '100%',
          position: 'center',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
        >
          {/* Score Range Label Box */}
          <FormControl sx={{ width: "50%" }}>
            <InputLabel id="score-range-label">Score Range</InputLabel>
              <Select
                labelId="score-range-label"
                id="score-range-select"
                value={value}
                onChange={scoreRangeHandleChange}
                label="Score Range"
                sx = {{ 
                  "& .MuiInputBase-input": {
                    color: "black", // Text color inside the Select box
                  }, 
                  "& .MuiSelect-icon": {
                    color: "black", // Select icon color
                  },
                }}
              >
              <MenuItem value={5}>5-Point Scoring</MenuItem>
              <MenuItem value={10}>10-Point Scoring</MenuItem>
            </Select>
            {/* <FormLabel id="score-range-label">Score Range</FormLabel>
            <RadioGroup
              aria-labelledby="score-range-label"
              name="controlled-radio-buttons-group"
              value={value}
              onChange={scoreRangeHandleChange}
            >
              <FormControlLabel value="5" control={<Radio />} label="5-Point Scoring" />
              <FormControlLabel value="10" control={<Radio />} label="10-Point Scoring" />
          </RadioGroup> */}
          </FormControl>

          <Typography>Role-Based Matrix</Typography>
          <FormControlLabel
            control={<Switch checked={RoleBasedChecked} onChange={roleBasedHandleToggle} sx={{'& .MuiSwitch-track': {
              borderRadius: '20px',
              backgroundColor: RoleBasedChecked ? 'blue' : 'red',
              opacity: 1,
            },}} />}
            label={RoleBasedChecked ? "Enabled" : "Disabled"}
          />
          
          {/* Data Entry Assignment Method Label Box */}
          <InputLabel id="data-entry-label">Data Entry Assignment Method</InputLabel>
          <FormControl sx={{ border: '1px solid lightgray', borderRadius: '50px', width: "50%"}}>
              <Select
                labelId="data-entry-label"
                id="data-entry-select"
                value={0}
                label="Data Entry Assignment Method"
                variant="outlined"
                sx={{ borderRadius: '50px'}}
              >
              <MenuItem value={10}>Ten</MenuItem>
              <MenuItem value={20}>Twenty</MenuItem>
              <MenuItem value={30}>Thirty</MenuItem>
            </Select>
          </FormControl>

          <Typography>Anonymous Entry</Typography>
          <FormControlLabel
            control={<Switch checked={AnonymousEntryChecked} onChange={anonymousEntryHandleToggle} sx={{'& .MuiSwitch-track': {
              borderRadius: '20px',
              backgroundColor: AnonymousEntryChecked ? 'blue' : 'red',
              opacity: 1,
            },}} />}
            label={AnonymousEntryChecked ? "Enabled" : "Disabled"}
          />
        </Box>

        {/* This Box is for the Global Category Multipliers */}
        <Box
        id='GlobalCategoryMultipliersBox'
        sx={{
          backgroundColor: 'white',
          width: '50%',
          height: '85%',
          position: 'center',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
        >
          {/* TO DO: implement CARVER Global Category Multipliers (i.e., 1.2x Criticality, 0.8x Recognizability, etc) */}
        </Box>
      </Box>

      {/* Space between the Boxes */}
      <Box sx={{ width: '20px' }} /> 

      {/* This Box is the Right-Half of the screen and it deals with managing participants */}
      <Box
        id='manageParticipantsBox'
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
          flexDirection: 'column'
        }}
      >
        Work in progress...
      </Box>
    </Box>
  );
};

export default CreateMatrix;